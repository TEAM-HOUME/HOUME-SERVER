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
     */
    public Optional<Long> parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = NUMBER.matcher(text);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String digits = matcher.group().replace(",", "");
        Matcher decimal = TRAILING_DECIMAL.matcher(digits);
        if (decimal.matches()) {
            digits = decimal.group(1);
        } else {
            digits = digits.replace(".", "");
        }

        try {
            return Optional.of(Long.parseLong(digits));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
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
