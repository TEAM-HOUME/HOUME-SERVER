package or.sopt.houme.domain.coupang.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.coupang.domain.CoupangProductSearchResult;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupang_products")
@Comment("쿠팡 파트너스 검색 상품 캐시")
public class CoupangProductJpaEntity extends BaseEntity {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String coupangProductId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 1000)
    private String imageUrl;

    @Column(nullable = false, length = 1000)
    private String productUrl;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountRate;

    /** 쿠팡 파트너스 API의 판매가·할인율을 기준으로 100원 단위 반올림한 추정 원가입니다. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedOriginalPrice;

    @Column(name = "title_embedding", columnDefinition = "text")
    @Comment("상품명 임베딩 벡터 (pgvector 형식, 배치로 채움)")
    private String titleEmbedding;

    @Column(name = "image_embedding", columnDefinition = "text")
    @Comment("상품 이미지 임베딩 벡터 (pgvector 형식, 배치로 채움)")
    private String imageEmbedding;

    private CoupangProductJpaEntity(CoupangProductSearchResult result) {
        apply(result);
    }

    public static CoupangProductJpaEntity from(CoupangProductSearchResult result) {
        return new CoupangProductJpaEntity(result);
    }

    public void apply(CoupangProductSearchResult result) {
        this.coupangProductId = result.productId();
        this.name = result.productName();
        this.imageUrl = result.productImage();
        this.productUrl = result.productUrl();
        this.currentPrice = result.productPrice();
        this.discountRate = result.productDiscountRate();
        this.estimatedOriginalPrice = estimateOriginalPrice(result.productPrice(), result.productDiscountRate());
    }

    private BigDecimal estimateOriginalPrice(BigDecimal currentPrice, BigDecimal discountRate) {
        if (currentPrice == null || currentPrice.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        if (discountRate == null || discountRate.signum() <= 0 || discountRate.compareTo(HUNDRED) >= 0) {
            return roundToHundred(currentPrice);
        }

        BigDecimal estimatedPrice = currentPrice.multiply(HUNDRED)
                .divide(HUNDRED.subtract(discountRate), 10, RoundingMode.HALF_UP);
        return roundToHundred(estimatedPrice);
    }

    private BigDecimal roundToHundred(BigDecimal price) {
        return price.setScale(-2, RoundingMode.HALF_UP);
    }
}
