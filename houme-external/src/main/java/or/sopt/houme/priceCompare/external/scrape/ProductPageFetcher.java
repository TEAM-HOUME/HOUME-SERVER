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
import java.util.Optional;

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

    // 봇 차단 우회 목적이 아니라, 기본 UA 로는 정상 HTML 대신 안내 페이지를 주는 몰이 있어 지정한다.
    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

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
                target = redirect.get();
                continue;
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("상품 페이지 응답 실패: url={}, status={}", target, response.statusCode());
                throw new PriceCompareException(ErrorCode.PRODUCT_PAGE_FETCH_FAILED);
            }
            requireHtml(response, target);
            return parse(response, target);
        }

        log.warn("상품 페이지 리다이렉트 한도 초과: url={}", url);
        throw new PriceCompareException(ErrorCode.PRODUCT_PAGE_FETCH_FAILED);
    }

    private HttpResponse<InputStream> send(URI target) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(target)
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
                .GET()
                .build();
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

    private Optional<URI> redirectTarget(HttpResponse<InputStream> response, URI current) {
        int status = response.statusCode();
        if (status < 300 || status >= 400) {
            return Optional.empty();
        }
        return response.headers().firstValue("location").map(current::resolve);
    }

    /** 이미지·PDF URL 이 들어와도 파싱을 시도하지 않도록 컨텐츠 타입을 먼저 거른다. */
    private void requireHtml(HttpResponse<InputStream> response, URI target) {
        String contentType = response.headers().firstValue("content-type")
                .map(value -> value.toLowerCase(Locale.ROOT))
                .orElse("");
        if (!contentType.isBlank() && !contentType.contains("html")) {
            log.warn("HTML 이 아닌 응답: url={}, contentType={}", target, contentType);
            throw new PriceCompareException(ErrorCode.PRODUCT_METADATA_PARSE_FAILED);
        }
    }

    /**
     * 응답 본문을 상한까지만 읽어 파싱한다.
     * charset 은 응답 헤더 → HTML 내 meta 태그 순으로 jsoup 이 판단하도록 넘긴다(구형 몰의 EUC-KR 대응).
     */
    private Document parse(HttpResponse<InputStream> response, URI target) {
        try (InputStream body = response.body()) {
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

    private Optional<String> extractCharset(String contentType) {
        int at = contentType.toLowerCase(Locale.ROOT).indexOf("charset=");
        if (at < 0) {
            return Optional.empty();
        }
        String charset = contentType.substring(at + "charset=".length()).split(";")[0].trim().replace("\"", "");
        return charset.isBlank() ? Optional.empty() : Optional.of(charset);
    }
}
