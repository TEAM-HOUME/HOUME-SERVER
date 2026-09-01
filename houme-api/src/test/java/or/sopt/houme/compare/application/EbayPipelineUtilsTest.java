package or.sopt.houme.compare.application;

import or.sopt.houme.compare.domain.EbayCandidate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;

class EbayPipelineUtilsTest {

    private final EbayPipelineUtils utils = new EbayPipelineUtils();

    @Test
    @DisplayName("카테고리가 허용 목록에 있으면 하드필터 통과한다")
    void passesHardFilter_allowedCategory_passes() {
        EbayCandidate item = candidate("id", 0.0, null, List.of("3197"));
        assertThat(utils.passesHardFilter(item, Set.of("3197", "3198"))).isTrue();
    }

    @Test
    @DisplayName("카테고리가 허용 목록에 없으면 하드필터 차단된다")
    void passesHardFilter_unknownCategory_blocked() {
        EbayCandidate item = candidate("id", 0.0, null, List.of("9999"));
        assertThat(utils.passesHardFilter(item, Set.of("3197"))).isFalse();
    }

    @Test
    @DisplayName("categoryIds가 null이면 하드필터 차단된다")
    void passesHardFilter_nullCategories_blocked() {
        EbayCandidate item = new EbayCandidate("id", "title", 0.0, null, null, null);
        assertThat(utils.passesHardFilter(item, Set.of("3197"))).isFalse();
    }

    @Test
    @DisplayName("parsePrice — priceUsd 값을 그대로 반환한다")
    void parsePrice_returnsUsdPrice() {
        EbayCandidate item = candidate("id", 199.99, null, List.of());
        assertThat(utils.parsePrice(item)).isEqualTo(199.99, offset(0.001));
    }

    @Test
    @DisplayName("parsePrice — priceUsd가 0이면 0.0을 반환한다")
    void parsePrice_zeroPriceReturnsZero() {
        EbayCandidate item = candidate("id", 0.0, null, List.of());
        assertThat(utils.parsePrice(item)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("코사인 유사도 — 동일 벡터는 1.0이다")
    void cosineSimilarity_identicalVectors_returnsOne() {
        List<Double> v = List.of(1.0, 2.0, 3.0);
        assertThat(utils.cosineSimilarity(v, v)).isEqualTo(1.0, offset(1e-9));
    }

    @Test
    @DisplayName("코사인 유사도 — 직교 벡터는 0.0이다")
    void cosineSimilarity_orthogonalVectors_returnsZero() {
        List<Double> a = List.of(1.0, 0.0);
        List<Double> b = List.of(0.0, 1.0);
        assertThat(utils.cosineSimilarity(a, b)).isEqualTo(0.0, offset(1e-9));
    }

    @Test
    @DisplayName("코사인 유사도 — 제로 벡터는 0.0이다")
    void cosineSimilarity_zeroVector_returnsZero() {
        List<Double> zero = List.of(0.0, 0.0);
        List<Double> v    = List.of(1.0, 1.0);
        assertThat(utils.cosineSimilarity(zero, v)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("코사인 유사도 — 차원이 다르면 IllegalArgumentException을 던진다")
    void cosineSimilarity_differentDimensions_throwsException() {
        List<Double> a = List.of(1.0, 2.0, 3.0);
        List<Double> b = List.of(1.0, 2.0);
        assertThatThrownBy(() -> utils.cosineSimilarity(a, b))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("임베딩 차원이 다릅니다");
    }

    @Test
    @DisplayName("thumbnailUrl — thumbnailUrl 필드 값을 반환한다")
    void thumbnailUrl_returnsValue() {
        EbayCandidate item = candidate("id", 0.0, "https://img1", List.of());
        assertThat(utils.thumbnailUrl(item)).isEqualTo("https://img1");
    }

    @Test
    @DisplayName("thumbnailUrl — thumbnailUrl이 null이면 null 반환한다")
    void thumbnailUrl_nullUrl_returnsNull() {
        EbayCandidate item = candidate("id", 0.0, null, List.of());
        assertThat(utils.thumbnailUrl(item)).isNull();
    }

    private EbayCandidate candidate(String itemId, double priceUsd, String thumbnailUrl, List<String> categoryIds) {
        return new EbayCandidate(itemId, "title", priceUsd, thumbnailUrl, "https://ebay.com/" + itemId, categoryIds);
    }
}
