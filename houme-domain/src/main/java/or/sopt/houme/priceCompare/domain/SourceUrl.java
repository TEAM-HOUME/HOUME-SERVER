package or.sopt.houme.priceCompare.domain;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
            List.of("gclid", "fbclid", "msclkid", "igshid", "spm", "srsltid", "_ga", "_gl", "_fromlogger");
    private static final int MAX_URL_LENGTH = 2048;

    /** 우리 앱이 원본 URL을 감싸 보내는 딥링크 형태. 이 접두사로 시작할 때만 언랩한다. */
    private static final List<String> DEEP_LINK_PREFIXES =
            List.of("https://houme.kr/", "http://houme.kr/", "houme.kr/");

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

        return new SourceUrl(rebuild(uri, scheme, host));
    }

    /**
     * `houme.kr/https://ohou.se/...` 처럼 원본 URL을 뒤에 붙인 딥링크에서 원본만 떼어낸다.
     * 딥링크 파싱은 프론트 라우팅 책임이지만, 서버도 그대로 받았을 때 동작하도록 방어한다.
     *
     * <p>"어디든 http 가 나오면 거기서 자른다"로 두면 딥링크가 아닌 정상 주소까지 망친다 —
     * 쇼핑몰은 `?returnUrl=https://...` 같은 파라미터에 절대 URL을 자주 싣고,
     * 그걸 잘라내면 유저가 요청하지 않은 페이지를 긁어서 응답하게 된다.
     * 그래서 언랩은 "우리 호스트가 원본 URL을 감싼 형태"일 때만 한다.
     */
    private static String stripDeepLinkPrefix(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        for (String prefix : DEEP_LINK_PREFIXES) {
            if (!lower.startsWith(prefix)) {
                continue;
            }
            String embedded = input.substring(prefix.length());
            String embeddedLower = embedded.toLowerCase(Locale.ROOT);
            if (embeddedLower.startsWith("https://") || embeddedLower.startsWith("http://")) {
                return embedded;
            }
        }
        return input;
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

    private static String lowerCase(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }
}
