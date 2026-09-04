package or.sopt.houme.priceCompare.external.scrape;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/**
 * 상품 페이지 HTML을 내려받아 jsoup {@link Document} 로 만들어준다.
 *
 * <p>상대 남의 서버를 긁는 일이라 방어 조건이 여러 겹이다 —
 * 타임아웃, 응답 크기 상한, HTML 여부 확인, 리다이렉트 홉마다 SSRF 재검증.
 * 자동 리다이렉트를 끄고 직접 따라가는 이유가 마지막 항목 때문이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductPageFetcher {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(7);
    private static final int MAX_REDIRECTS = 3;
    private static final int MAX_BODY_BYTES = 3 * 1024 * 1024;

    /**
     * 브라우저가 실제로 보내는 요청 헤더 일습.
     *
     * <p>대형 몰(오늘의집=Akamai 등)은 헤더가 하나라도 빠지면 403 을 준다.
     * 실측 결과 UA 만으로는 403, 아래 조합을 모두 갖췄을 때만 200 이며,
     * {@code sec-ch-ua}/{@code Sec-Fetch-*}/{@code Accept-Encoding} 중 하나만 빼도 다시 403 이 된다.
     * 정상 브라우저와 같은 요청을 보낸다는 의미이지, 인증이나 접근 제어를 우회하는 것이 아니다.
     */
    private static final Map<String, String> BROWSER_HEADERS = Map.ofEntries(
            Map.entry("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"),
            Map.entry("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,"
                    + "image/avif,image/webp,*/*;q=0.8"),
            Map.entry("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8"),
            // JDK HttpClient 는 자동 압축 해제를 하지 않으므로 gzip 만 요청하고 아래에서 직접 푼다.
            Map.entry("Accept-Encoding", "gzip"),
            Map.entry("sec-ch-ua", "\"Chromium\";v=\"126\", \"Not;A=Brand\";v=\"24\""),
            Map.entry("sec-ch-ua-mobile", "?0"),
            Map.entry("sec-ch-ua-platform", "\"macOS\""),
            Map.entry("Sec-Fetch-Dest", "document"),
            Map.entry("Sec-Fetch-Mode", "navigate"),
            // 주소창에 직접 입력한 이동을 의미한다. cross-site 로 보내면 오히려 403 이 난다.
            Map.entry("Sec-Fetch-Site", "none"),
            Map.entry("Sec-Fetch-User", "?1"),
            Map.entry("Upgrade-Insecure-Requests", "1")
    );

    private final SourceUrlValidator sourceUrlValidator;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    public Document fetch(String url) {
        URI target = URI.create(url);

        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            sourceUrlValidator.validate(target);
            HttpResponse<InputStream> response = send(target);

            Optional<URI> redirect = redirectTarget(response, target);
            if (redirect.isPresent()) {
                discard(response);
                target = redirect.get();
                continue;
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                discard(response);
                log.warn("상품 페이지 응답 실패: url={}, status={}", target, response.statusCode());
                throw new PriceCompareException(ErrorCode.PRODUCT_PAGE_FETCH_FAILED);
            }
            if (!isHtml(response)) {
                discard(response);
                log.warn("HTML 이 아닌 응답: url={}, contentType={}", target, contentType(response));
                throw new PriceCompareException(ErrorCode.PRODUCT_METADATA_PARSE_FAILED);
            }
            return parse(response, target);
        }

        log.warn("상품 페이지 리다이렉트 한도 초과: url={}", url);
        throw new PriceCompareException(ErrorCode.PRODUCT_PAGE_FETCH_FAILED);
    }

    private HttpResponse<InputStream> send(URI target) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(target)
                .timeout(REQUEST_TIMEOUT)
                .GET();
        BROWSER_HEADERS.forEach(builder::header);
        HttpRequest request = builder.build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            log.warn("상품 페이지 요청 실패: url={}, message={}", target, e.getMessage());
            throw new PriceCompareException(ErrorCode.PRODUCT_PAGE_FETCH_FAILED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PriceCompareException(ErrorCode.PRODUCT_PAGE_FETCH_FAILED);
        }
    }

    /**
     * 3xx 응답의 다음 홉을 구한다.
     *
     * <p>{@code Location} 은 남의 서버가 넣은 값이라 공백·제어문자가 섞여 URI 로 파싱되지 않는 경우가 있다.
     * 그대로 두면 {@code resolve} 의 {@link IllegalArgumentException} 이 밖으로 나가 502 대신 500 이 되므로,
     * 여기서 삼켜 "리다이렉트 없음"으로 돌려보내고 호출부의 상태 코드 검사에서 조회 실패로 처리하게 한다.
     */
    private Optional<URI> redirectTarget(HttpResponse<InputStream> response, URI current) {
        int status = response.statusCode();
        if (status < 300 || status >= 400) {
            return Optional.empty();
        }
        return response.headers().firstValue("location").flatMap(location -> {
            try {
                return Optional.of(current.resolve(location));
            } catch (IllegalArgumentException e) {
                log.warn("리다이렉트 Location 헤더를 해석할 수 없음: url={}, location={}", current, location);
                return Optional.empty();
            }
        });
    }

    /**
     * 이미지·PDF URL 이 들어와도 파싱을 시도하지 않도록 컨텐츠 타입을 먼저 거른다.
     *
     * <p>헤더가 아예 없는 응답도 HTML 로 보지 않는다 — 정상 몰은 예외 없이 Content-Type 을 보내므로,
     * 없는 쪽을 통과시키면 얻는 것 없이 "HTML 아닌 응답은 파싱하지 않는다"는 전제만 깨진다.
     */
    private boolean isHtml(HttpResponse<InputStream> response) {
        return contentType(response).contains("html");
    }

    private String contentType(HttpResponse<InputStream> response) {
        return response.headers().firstValue("content-type")
                .map(value -> value.toLowerCase(Locale.ROOT))
                .orElse("");
    }

    /**
     * 파싱하지 않고 버리는 응답의 본문을 닫는다.
     *
     * <p>{@link HttpResponse.BodyHandlers#ofInputStream()} 은 본문을 닫거나 끝까지 읽을 때만
     * 커넥션을 풀에 반환한다. 리다이렉트로 넘어가거나 예외로 빠지는 경로에서 닫지 않으면
     * 요청이 쌓일수록 커넥션이 고갈된다. 닫기 실패는 원래의 실패 원인을 가리지 않도록 로그만 남긴다.
     */
    private void discard(HttpResponse<InputStream> response) {
        try {
            response.body().close();
        } catch (IOException e) {
            log.warn("응답 본문 스트림 닫기 실패: message={}", e.getMessage());
        }
    }

    /**
     * 응답 본문을 상한까지만 읽어 파싱한다.
     * charset 은 응답 헤더 → HTML 내 meta 태그 순으로 jsoup 이 판단하도록 넘긴다(구형 몰의 EUC-KR 대응).
     */
    private Document parse(HttpResponse<InputStream> response, URI target) {
        try (InputStream body = decode(response)) {
            byte[] bytes = body.readNBytes(MAX_BODY_BYTES);
            String charset = response.headers().firstValue("content-type")
                    .flatMap(this::extractCharset)
                    .orElse(null);
            return Jsoup.parse(new ByteArrayInputStream(bytes), charset, target.toString());
        } catch (IOException e) {
            log.warn("상품 페이지 본문 읽기 실패: url={}, message={}", target, e.getMessage());
            throw new PriceCompareException(ErrorCode.PRODUCT_PAGE_FETCH_FAILED);
        }
    }

    /** gzip 을 요청했으므로 압축 응답이면 직접 푼다. JDK HttpClient 는 자동 해제를 하지 않는다. */
    private InputStream decode(HttpResponse<InputStream> response) throws IOException {
        boolean gzipped = response.headers().firstValue("content-encoding")
                .map(encoding -> encoding.toLowerCase(Locale.ROOT).contains("gzip"))
                .orElse(false);
        return gzipped ? new GZIPInputStream(response.body()) : response.body();
    }

    private Optional<String> extractCharset(String contentType) {
        int at = contentType.toLowerCase(Locale.ROOT).indexOf("charset=");
        if (at < 0) {
            return Optional.empty();
        }
        String charset = contentType.substring(at + "charset=".length()).split(";")[0].trim().replace("\"", "");
        return charset.isBlank() ? Optional.empty() : Optional.of(charset);
    }
}
