package or.sopt.houme.domain.coupang;

import or.sopt.houme.domain.coupang.seed.CoupangKeywordCatalog;
import or.sopt.houme.domain.coupang.service.CoupangSeedKeyword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoupangKeywordCatalogTest {

    @Test
    @DisplayName("기본 쿠팡 배치 키워드는 10개 가구 카테고리를 포함한 100개로 구성된다")
    void defaultKeywordsContainOneHundredFurnitureQueries() {
        List<CoupangSeedKeyword> keywords = CoupangKeywordCatalog.defaults();

        assertThat(keywords)
                .hasSize(100)
                .extracting(CoupangSeedKeyword::keyword)
                .doesNotHaveDuplicates();
        assertThat(keywords)
                .extracting(CoupangSeedKeyword::category)
                .contains(
                        "SOFA", "TABLE", "BED", "STORAGE", "WARDROBE",
                        "DESK", "BATHROOM", "LIVING", "KIDS", "OUTDOOR"
                );
    }
}
