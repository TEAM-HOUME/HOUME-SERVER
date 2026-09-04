package or.sopt.houme.priceCompare.external.scrape;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 화면에 보이는 가격 문자열을 숫자와 통화로 분해한다.
 * ({@code "459,000원"} → 459000 / KRW, {@code "$1,299.00"} → 1299 / USD)
 */
@Component
public class PriceTextParser {

    private static final Pattern NUMBER = Pattern.compile("[0-9][0-9,.]*");
    private static final Pattern TRAILING_DECIMAL = Pattern.compile("^(\\d+)\\.\\d{1,2}$");

    private static final String KRW = "KRW";
    private static final String USD = "USD";

    /**
     * 문자열에서 첫 번째 금액을 뽑는다. 소수점 이하는 버린다(원 단위 비교가 기준이라 의미가 없다).
     *
     * <p>단, `%` 가 뒤따르는 숫자는 후보에서 뺀다 — `"10% 할인 89,900원"` 처럼 할인율이 앞에 오는 표기가
     * 실제 몰에 흔하고, 그대로 첫 숫자를 잡으면 10 원이 가격으로 응답에 실린다.
     * JSON-LD/OG 값은 정제되어 있어 무해하지만 화면 텍스트를 그대로 넘기는 폴백 경로에서 문제가 된다.
     *
     * <p>여러 후보 중 "자릿수가 큰 쪽"이 아니라 여전히 "앞에 나온 쪽"을 택한다.
     * `"정가 1,290,000원 판매가 890,000원"` 처럼 정가가 먼저 오는 표기에서 큰 값을 고르면
     * 판매가가 아닌 정가를 집게 되기 때문이다.
     */
    public Optional<Long> parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        Optional<String> candidate = firstNonPercentNumber(text);
        if (candidate.isEmpty()) {
            return Optional.empty();
        }

        String digits = candidate.get().replace(",", "");
        Matcher decimal = TRAILING_DECIMAL.matcher(digits);
        if (decimal.matches()) {
            digits = decimal.group(1);
        } else {
            digits = digits.replace(".", "");
        }

        try {
            long amount = Long.parseLong(digits);
            // 0원은 "가격 없음"이다 — 무신사처럼 SSR 전 초기값 0 을 메타 태그에 그대로 박아두는 몰이 있고,
            // 이걸 가격으로 받으면 비교 대상이 되지 않는 값으로 성공 응답이 나간다.
            return amount > 0 ? Optional.of(amount) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /** `%` 가 (공백을 건너뛰고) 뒤따르는 숫자는 할인율이므로 건너뛴다. */
    private Optional<String> firstNonPercentNumber(String text) {
        Matcher matcher = NUMBER.matcher(text);
        while (matcher.find()) {
            if (!isFollowedByPercent(text, matcher.end())) {
                return Optional.of(matcher.group());
            }
        }
        return Optional.empty();
    }

    private boolean isFollowedByPercent(String text, int from) {
        for (int at = from; at < text.length(); at++) {
            char each = text.charAt(at);
            if (each == '%') {
                return true;
            }
            if (!Character.isWhitespace(each)) {
                return false;
            }
        }
        return false;
    }

    /**
     * 통화를 정한다. 페이지가 명시한 값이 항상 우선이고,
     * 없으면 기호/단위 → 도메인 순으로 추정한다.
     */
    public String resolveCurrency(String declaredCurrency, String priceText, String host) {
        if (declaredCurrency != null && !declaredCurrency.isBlank()) {
            return declaredCurrency.trim().toUpperCase(Locale.ROOT);
        }
        if (priceText != null) {
            if (priceText.contains("₩") || priceText.contains("원")) {
                return KRW;
            }
            if (priceText.contains("$")) {
                return USD;
            }
        }
        if (host != null && host.toLowerCase(Locale.ROOT).endsWith(".kr")) {
            return KRW;
        }
        return KRW;
    }
}
