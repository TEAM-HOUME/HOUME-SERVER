package or.sopt.houme.domain.coupang;

import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoupangProductJpaEntityTest {

    @Test
    @DisplayName("이미지 URL이 변경되면 기존 이미지 임베딩을 비워 재생성 대상으로 만든다")
    void clearsImageEmbeddingWhenImageUrlChanges() {
        CoupangProductJpaEntity product = CoupangProductJpaEntity.from(product("https://image-1"));
        product.updateImageEmbedding(List.of(0.1, 0.2));

        assertThat(product.needsImageEmbedding()).isFalse();

        product.apply(product("https://image-2"));

        assertThat(product.getImageEmbedding()).isNull();
        assertThat(product.needsImageEmbedding()).isTrue();
    }

    @Test
    @DisplayName("판매가와 할인율로 추정 원가를 계산해 100원 단위로 반올림한다")
    void estimatesOriginalPriceFromCurrentPriceAndDiscountRate() {
        CoupangProductJpaEntity product = CoupangProductJpaEntity.from(new CoupangProductSearchResult(
                "1",
                "테스트 소파",
                new BigDecimal("15400"),
                new BigDecimal("23"),
                "https://image",
                "https://product"
        ));

        assertThat(product.getDiscountRate()).isEqualByComparingTo("23");
        assertThat(product.getEstimatedOriginalPrice()).isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("할인율이 없으면 판매가를 100원 단위로 반올림해 추정 원가로 저장한다")
    void roundsCurrentPriceWhenDiscountRateIsZero() {
        CoupangProductJpaEntity product = CoupangProductJpaEntity.from(new CoupangProductSearchResult(
                "1",
                "테스트 소파",
                new BigDecimal("15450"),
                BigDecimal.ZERO,
                "https://image",
                "https://product"
        ));

        assertThat(product.getEstimatedOriginalPrice()).isEqualByComparingTo("15500");
    }

    private CoupangProductSearchResult product(String imageUrl) {
        return new CoupangProductSearchResult(
                "1", "테스트 소파", new BigDecimal("15400"), BigDecimal.ZERO, imageUrl, "https://product"
        );
    }
}
