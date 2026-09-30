package or.sopt.houme.priceCompare.domain;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 유저가 입력한 외부 상품 URL을 스크래핑에 쓸 수 있는 형태로 정규화한 값 객체.
 *
 * <p>유저 입력은 우리가 기대하는 모양으로 오지 않는다 — 프로토콜이 빠져 있거나(`ohou.se/...`),
 * 딥링크 형태로 감싸여 있거나(`houme.kr/https://...`), 광고 트래킹 파라미터가 잔뜩 붙어 있다.
 * 그 흔들림을 여기서 흡수해, 이후 단계는 항상 정상적인 절대 URL만 다루게 한다.
 */
public record SourceUrl(String value) {

    private static final List<String> ALLOWED_SCHEMES = List.of("http", "https");
    private static final List<String> TRACKING_PARAM_PREFIXES = List.of("utm_");
    private static final List<String> TRACKING_PARAM_NAMES =
            List.of("gclid", "fbclid", "igshid", "spm", "srsltid", "_fromlogger");
    private static final int MAX_URL_LENGTH = 2048;

    private static final List<String> COUPANG_HOSTS = List.of("coupang.com", "www.coupang.com", "m.coupang.com");
    private static final Pattern COUPANG_PRODUCT_PATH = Pattern.compile("^/v[pm]/products/(\\d+)/?$");
    private static final String COUPANG_CANONICAL_PREFIX = "https://www.coupang.com/vp/products/";

    public static SourceUrl normalize(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            throw new PriceCompareException(ErrorCode.INVALID_PRODUCT_URL);
        }

        String candidate = stripDeepLinkPrefix(rawInput.trim());
        candidate = ensureScheme(candidate);

        if (candidate.length() > MAX_URL_LENGTH) {
            throw new PriceCompareException(ErrorCode.INVALID_PRODUCT_URL);
        }

        URI uri = parse(candidate);
        String scheme = lowerCase(uri.getScheme());
        String host = lowerCase(uri.getHost());
        if (!ALLOWED_SCHEMES.contains(scheme) || host == null || host.isBlank()) {
            throw new PriceCompareException(ErrorCode.INVALID_PRODUCT_URL);
        }

        String coupangProductId = extractCoupangProductId(host, uri.getRawPath());
        if (coupangProductId != null) {
            return new SourceUrl(COUPANG_CANONICAL_PREFIX + coupangProductId);
        }
        return new SourceUrl(rebuild(uri, scheme, host));
    }

    /**
     * 쿠팡 상품 URL 이면 상품 ID 를 돌려준다. 자체 카탈로그(쿠팡 파트너스 수집분) 조회 키로 쓴다.
     * {@link #normalize} 를 거친 값은 항상 정규형이라 이 판정도 정규형 기준으로 한다.
     */
    public Optional<String> coupangProductId() {
        if (!value.startsWith(COUPANG_CANONICAL_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(value.substring(COUPANG_CANONICAL_PREFIX.length()));
    }

    /**
     * 쿠팡은 공유 경로마다 같은 상품이 다른 URL 로 온다
     * (`m.coupang.com/vm/products/{id}`, `?itemId=..&vendorItemId=..&clickEventId=..` 등).
     * 모바일 페이지는 서버 요청을 403 으로 막았고, {@code itemId} 쿼리가 붙은 페이지는 같은 상품인데도 가격이 빠진 채 파싱됐다.
     * 그래서 상품 ID 만 남긴 데스크톱 URL 하나로 모은다 — 카탈로그 조회 키와 진행 중 job 재활용 키도 이 값 하나가 된다.
     * 옵션(itemId) 단위 가격 대신 상품 대표가를 쓰게 되는 점은 감수한다.
     */
    private static String extractCoupangProductId(String host, String rawPath) {
        if (!COUPANG_HOSTS.contains(host) || rawPath == null) {
            return null;
        }
        Matcher matcher = COUPANG_PRODUCT_PATH.matcher(rawPath);
        return matcher.matches() ? matcher.group(1) : null;
    }

    /**
     * `houme.kr/https://ohou.se/...` 처럼 원본 URL을 뒤에 붙인 딥링크에서 원본만 떼어낸다.
     * 딥링크 파싱은 프론트 라우팅 책임이지만, 서버도 그대로 받았을 때 동작하도록 방어한다.
     */
    private static String stripDeepLinkPrefix(String input) {
        int httpsAt = input.indexOf("https://", 1);
        int httpAt = input.indexOf("http://", 1);
        int embeddedAt = min(httpsAt, httpAt);
        return embeddedAt > 0 ? input.substring(embeddedAt) : input;
    }

    private static String ensureScheme(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return (lower.startsWith("http://") || lower.startsWith("https://")) ? input : "https://" + input;
    }

    private static URI parse(String candidate) {
        try {
            return new URI(candidate);
        } catch (URISyntaxException e) {
            throw new PriceCompareException(ErrorCode.INVALID_PRODUCT_URL);
        }
    }

    /** 프래그먼트와 트래킹 파라미터를 걷어내고 host 를 소문자로 통일해 다시 조립한다. */
    private static String rebuild(URI uri, String scheme, String host) {
        StringBuilder rebuilt = new StringBuilder(scheme).append("://").append(host);
        if (uri.getPort() != -1) {
            rebuilt.append(':').append(uri.getPort());
        }
        rebuilt.append(uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/" : uri.getRawPath());

        String query = removeTrackingParams(uri.getRawQuery());
        if (!query.isBlank()) {
            rebuilt.append('?').append(query);
        }
        return rebuilt.toString();
    }

    private static String removeTrackingParams(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return "";
        }
        return Arrays.stream(rawQuery.split("&"))
                .filter(param -> !param.isBlank())
                .filter(param -> !isTrackingParam(param))
                .collect(Collectors.joining("&"));
    }

    private static boolean isTrackingParam(String param) {
        String name = lowerCase(param.split("=", 2)[0]);
        if (name == null) {
            return false;
        }
        return TRACKING_PARAM_NAMES.contains(name)
                || TRACKING_PARAM_PREFIXES.stream().anyMatch(name::startsWith);
    }

    private static int min(int left, int right) {
        if (left < 0) {
            return right;
        }
        if (right < 0) {
            return left;
        }
        return Math.min(left, right);
    }

    private static String lowerCase(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }
}
