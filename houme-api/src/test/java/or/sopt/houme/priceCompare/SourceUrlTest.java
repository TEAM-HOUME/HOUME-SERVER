package or.sopt.houme.priceCompare;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.PriceCompareException;
import or.sopt.houme.priceCompare.domain.SourceUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SourceUrl 정규화")
class SourceUrlTest {

    @Test
    @DisplayName("프로토콜이 빠진 입력에 https 를 붙인다")
    void 프로토콜이_빠진_입력에_https_를_붙인다() {
        SourceUrl url = SourceUrl.normalize("ohou.se/productions/123");

        assertThat(url.value()).isEqualTo("https://ohou.se/productions/123");
    }

    @Test
    @DisplayName("houme.kr 딥링크로 감싸인 입력에서 원본 URL만 떼어낸다")
    void houme_딥링크에서_원본_URL만_떼어낸다() {
        SourceUrl url = SourceUrl.normalize("https://houme.kr/https://ohou.se/productions/123");

        assertThat(url.value()).isEqualTo("https://ohou.se/productions/123");
    }

    @Test
    @DisplayName("트래킹 파라미터는 제거하고 상품 식별에 필요한 파라미터는 남긴다")
    void 트래킹_파라미터만_제거한다() {
        SourceUrl url = SourceUrl.normalize(
                "https://ohou.se/productions/123?utm_source=kakao&color=beige&gclid=abc123");

        assertThat(url.value()).isEqualTo("https://ohou.se/productions/123?color=beige");
    }

    @Test
    @DisplayName("프래그먼트를 제거하고 host 를 소문자로 통일한다")
    void 프래그먼트_제거하고_host_를_소문자로_통일한다() {
        SourceUrl url = SourceUrl.normalize("https://OHOU.SE/productions/123#reviews");

        assertThat(url.value()).isEqualTo("https://ohou.se/productions/123");
    }

    @Test
    @DisplayName("경로가 없으면 루트 경로로 채운다")
    void 경로가_없으면_루트로_채운다() {
        SourceUrl url = SourceUrl.normalize("https://ohou.se");

        assertThat(url.value()).isEqualTo("https://ohou.se/");
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @DisplayName("쿠팡 상품 URL 은 공유 경로와 무관하게 상품 ID 만 남긴 데스크톱 URL 로 모은다")
    @ValueSource(strings = {
            "https://www.coupang.com/vp/products/7335597976",
            "https://www.coupang.com/vp/products/7335597976?itemId=18855165010&vendorItemId=86960985090",
            "https://m.coupang.com/vm/products/7335597976?itemId=18855165010",
            "coupang.com/vp/products/7335597976/?clickEventId=5328bd30",
            "https://WWW.COUPANG.COM/vp/products/7335597976#sdpReview"
    })
    void 쿠팡_상품_URL_을_정규형으로_모은다(String rawInput) {
        SourceUrl url = SourceUrl.normalize(rawInput);

        assertThat(url.value()).isEqualTo("https://www.coupang.com/vp/products/7335597976");
        assertThat(url.coupangProductId()).contains("7335597976");
    }

    @Test
    @DisplayName("쿠팡이어도 상품 상세가 아닌 경로는 쿼리를 보존하고 상품 ID 도 없다")
    void 쿠팡_상품_상세가_아니면_그대로_둔다() {
        SourceUrl url = SourceUrl.normalize("https://www.coupang.com/np/search?q=sofa");

        assertThat(url.value()).isEqualTo("https://www.coupang.com/np/search?q=sofa");
        assertThat(url.coupangProductId()).isEmpty();
    }

    @Test
    @DisplayName("쿠팡이 아닌 몰은 상품 ID 가 없다")
    void 쿠팡이_아니면_상품_ID_가_없다() {
        assertThat(SourceUrl.normalize("https://ohou.se/productions/123").coupangProductId()).isEmpty();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @DisplayName("URL 로 볼 수 없는 입력은 INVALID_PRODUCT_URL 로 거절한다")
    @ValueSource(strings = {"", "   ", "http://"})
    void 유효하지_않은_입력은_거절한다(String rawInput) {
        assertThatThrownBy(() -> SourceUrl.normalize(rawInput))
                .isInstanceOf(PriceCompareException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PRODUCT_URL);
    }

    @Test
    @DisplayName("null 입력은 INVALID_PRODUCT_URL 로 거절한다")
    void null_입력은_거절한다() {
        assertThatThrownBy(() -> SourceUrl.normalize(null))
                .isInstanceOf(PriceCompareException.class);
    }
}
