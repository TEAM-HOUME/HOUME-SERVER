package or.sopt.houme.compare.application;

import or.sopt.houme.compare.infrastructure.ebay.dto.EbaySearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class EbayPipelineUtilsTest {

    private final EbayPipelineUtils utils = new EbayPipelineUtils();

    @Test
    @DisplayName("카테고리가 허용 목록에 있으면 하드필터 통과한다")
    void passesHardFilter_allowedCategory_passes() {
        EbaySearchResponse.ItemSummary item = item("3197");
        assertThat(utils.passesHardFilter(item, Set.of("3197", "3198"))).isTrue();
    }

    @Test
    @DisplayName("카테고리가 허용 목록에 없으면 하드필터 차단된다")
    void passesHardFilter_unknownCategory_blocked() {
        EbaySearchResponse.ItemSummary item = item("9999");
        assertThat(utils.passesHardFilter(item, Set.of("3197"))).isFalse();
    }

    @Test
    @DisplayName("categories가 null이면 하드필터 차단된다")
    void passesHardFilter_nullCategories_blocked() {
        EbaySearchResponse.ItemSummary item = new EbaySearchResponse.ItemSummary(
                "id", "title", null, null, null, null);
        assertThat(utils.passesHardFilter(item, Set.of("3197"))).isFalse();
    }

    @Test
    @DisplayName("price가 null이면 parsePrice는 0.0을 반환한다")
    void parsePrice_nullPrice_returnsZero() {
        EbaySearchResponse.ItemSummary item = new EbaySearchResponse.ItemSummary(
                "id", "title", null, null, List.of(), null);
        assertThat(utils.parsePrice(item)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("price value가 숫자면 parsePrice가 파싱한다")
    void parsePrice_validValue_parsed() {
        EbaySearchResponse.ItemSummary item = new EbaySearchResponse.ItemSummary(
                "id", "title", new EbaySearchResponse.Price("199.99", "USD"), null, List.of(), null);
        assertThat(utils.parsePrice(item)).isEqualTo(199.99, offset(0.001));
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
    @DisplayName("thumbnailUrl — 첫 번째 이미지 URL을 반환한다")
    void thumbnailUrl_returnsFirst() {
        EbaySearchResponse.ItemSummary item = new EbaySearchResponse.ItemSummary(
                "id", "title", null,
                List.of(new EbaySearchResponse.ThumbnailImage("https://img1"),
                        new EbaySearchResponse.ThumbnailImage("https://img2")),
                List.of(), null);
        assertThat(utils.thumbnailUrl(item)).isEqualTo("https://img1");
    }

    @Test
    @DisplayName("thumbnailUrl — thumbnailImages가 null이면 null 반환한다")
    void thumbnailUrl_nullImages_returnsNull() {
        EbaySearchResponse.ItemSummary item = new EbaySearchResponse.ItemSummary(
                "id", "title", null, null, List.of(), null);
        assertThat(utils.thumbnailUrl(item)).isNull();
    }

    private EbaySearchResponse.ItemSummary item(String categoryId) {
        return new EbaySearchResponse.ItemSummary(
                "id", "title", null, null,
                List.of(new EbaySearchResponse.Category(categoryId, "category")),
                null);
    }
}
