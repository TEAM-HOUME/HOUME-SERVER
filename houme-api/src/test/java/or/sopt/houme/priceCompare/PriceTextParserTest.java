package or.sopt.houme.priceCompare;

import or.sopt.houme.priceCompare.external.scrape.PriceTextParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("가격 문자열 파싱")
class PriceTextParserTest {

    private final PriceTextParser priceTextParser = new PriceTextParser();

    @ParameterizedTest(name = "[{index}] \"{0}\" -> {1}")
    @DisplayName("화면 표기 그대로의 가격에서 숫자만 뽑는다")
    @CsvSource({
            "'459,000원', 459000",
            "'₩129,000', 129000",
            "'329000', 329000",
            "'$1,299.00', 1299",
            "'판매가 89,900원 (10% 할인)', 89900",
            "'10% 할인 89,900원', 89900",
            "'10 % 할인 89,900원', 89900",
            "'정가 1,290,000원 판매가 890,000원', 1290000"
    })
    void 가격_문자열에서_숫자를_뽑는다(String text, long expected) {
        assertThat(priceTextParser.parseAmount(text)).contains(expected);
    }

    @ParameterizedTest(name = "[{index}] \"{0}\"")
    @DisplayName("숫자가 없거나 0원인 문자열은 가격으로 보지 않는다")
    @CsvSource({"'품절'", "'가격문의'", "'   '", "'최대 30% 할인'", "'0'", "'0원'"})
    void 숫자가_없으면_비어있는_값을_반환한다(String text) {
        assertThat(priceTextParser.parseAmount(text)).isEmpty();
    }

    @Test
    @DisplayName("null 입력은 비어있는 값을 반환한다")
    void null_입력은_비어있는_값을_반환한다() {
        assertThat(priceTextParser.parseAmount(null)).isEmpty();
    }

    @Test
    @DisplayName("페이지가 통화를 명시하면 그 값을 그대로 쓴다")
    void 명시된_통화를_우선한다() {
        assertThat(priceTextParser.resolveCurrency("usd", "1299", "example.com")).isEqualTo("USD");
    }

    @Test
    @DisplayName("통화 표기가 없으면 기호와 단위로 추정한다")
    void 기호와_단위로_통화를_추정한다() {
        assertThat(priceTextParser.resolveCurrency(null, "459,000원", "example.com")).isEqualTo("KRW");
        assertThat(priceTextParser.resolveCurrency(null, "$1,299.00", "example.com")).isEqualTo("USD");
    }

    @Test
    @DisplayName("단서가 전혀 없으면 KRW 로 본다")
    void 단서가_없으면_KRW_로_본다() {
        assertThat(priceTextParser.resolveCurrency(null, "1299", "example.com")).isEqualTo("KRW");
    }
}
