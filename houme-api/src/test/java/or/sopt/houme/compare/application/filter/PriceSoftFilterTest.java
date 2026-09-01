package or.sopt.houme.compare.application.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceSoftFilterTest {

    private final PriceSoftFilter filter = new PriceSoftFilter();

    @Test
    @DisplayName("originalKrw가 null이면 항상 통과한다")
    void nullOriginal_alwaysPasses() {
        assertThat(filter.passes(null, 0)).isTrue();
        assertThat(filter.passes(null, 99_999_999)).isTrue();
    }

    @Test
    @DisplayName("저가 상품(50000원)은 허용 범위가 넓어 동일 가격은 통과한다")
    void lowPrice_samePricePasses() {
        assertThat(filter.passes(50_000.0, 50_000)).isTrue();
    }

    @Test
    @DisplayName("저가 상품(50000원)은 절반 가격도 통과한다")
    void lowPrice_halfPricePasses() {
        // t가 낮을수록 fLow가 최대(0.8) → minPrice = 50000 * 0.2 = 10000
        assertThat(filter.passes(50_000.0, 25_000)).isTrue();
    }

    @Test
    @DisplayName("저가 상품(50000원)은 2배 초과 가격은 차단된다")
    void lowPrice_doublePriceBlocked() {
        // fHigh 최대(0.5) → maxPrice = 50000 * 1.5 = 75000
        assertThat(filter.passes(50_000.0, 100_000)).isFalse();
    }

    @Test
    @DisplayName("고가 상품(3000000원)은 허용 범위가 좁아 40% 저렴하면 차단된다")
    void highPrice_40PercentCheaperBlocked() {
        // t가 높을수록 fLow 최소(0.6) → minPrice = 3000000 * 0.4 = 1200000
        assertThat(filter.passes(3_000_000.0, 1_000_000)).isFalse();
    }

    @Test
    @DisplayName("고가 상품(3000000원)은 20% 저렴하면 통과한다")
    void highPrice_20PercentCheaperPasses() {
        assertThat(filter.passes(3_000_000.0, 2_500_000)).isTrue();
    }
}
