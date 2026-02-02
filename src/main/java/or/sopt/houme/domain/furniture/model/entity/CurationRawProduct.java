package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(
        name = "curation_raw_products",
        indexes = {
                @Index(name = "idx_raw_source", columnList = "source"),
                @Index(name = "idx_raw_category", columnList = "category"),
                @Index(name = "idx_raw_fetched_at", columnList = "fetched_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_raw_source_category_product_id",
                        columnNames = {"source", "category", "product_id"}
                )
        }
)
@Comment("큐레이션 원본 수집 데이터를 저장하는 엔티티입니다")
public class CurationRawProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("식별자")
    private Long id;

    @Column(name = "source", nullable = false)
    @Comment("수집 출처")
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @Comment("카테고리")
    private SoozipCategory category;

    @Column(name = "product_id", nullable = false)
    @Comment("외부 상품 식별자")
    private Long productId;

    @Column(name = "product_image_url", columnDefinition = "varchar(2048)", nullable = false)
    @Comment("상품 이미지 URL")
    private String productImageUrl;

    @Column(name = "product_site_url", columnDefinition = "varchar(2048)", nullable = false)
    @Comment("상품 상세 URL")
    private String productSiteUrl;

    @Column(name = "product_name", nullable = false)
    @Comment("상품명")
    private String productName;

    @Column(name = "product_mall_name")
    @Comment("판매 몰 이름")
    private String productMallName;

    @Column(name = "brand")
    @Comment("브랜드")
    private String brand;

    @Column(name = "list_price")
    @Comment("정가")
    private Long listPrice;

    @Column(name = "discount_rate")
    @Comment("할인률(%)")
    private Integer discountRate;

    @Column(name = "discount_price")
    @Comment("할인가")
    private Long discountPrice;

    @Column(name = "base_shipping_fee")
    @Comment("기본 배송비")
    private Long baseShippingFee;

    @Column(name = "free_shipping_condition")
    @Comment("무료 배송 조건")
    private Long freeShippingCondition;

    @Column(name = "fetched_at", nullable = false)
    @Comment("수집 시각")
    private LocalDateTime fetchedAt;

    public static CurationRawProduct of(
            String source,
            SoozipCategory category,
            Long productId,
            String productImageUrl,
            String productSiteUrl,
            String productName,
            String productMallName,
            LocalDateTime fetchedAt
    ) {
        return CurationRawProduct.builder()
                .source(source)
                .category(category)
                .productId(productId)
                .productImageUrl(productImageUrl)
                .productSiteUrl(productSiteUrl)
                .productName(productName)
                .productMallName(productMallName)
                .fetchedAt(fetchedAt)
                .build();
    }

    public void updateFrom(
            String productImageUrl,
            String productSiteUrl,
            String productName,
            String productMallName,
            LocalDateTime fetchedAt
    ) {
        if (productImageUrl != null && !productImageUrl.isBlank()) {
            this.productImageUrl = productImageUrl;
        }
        if (productSiteUrl != null && !productSiteUrl.isBlank()) {
            this.productSiteUrl = productSiteUrl;
        }
        if (productName != null && !productName.isBlank()) {
            this.productName = productName;
        }
        if (productMallName != null && !productMallName.isBlank()) {
            this.productMallName = productMallName;
        }
        if (fetchedAt != null) {
            this.fetchedAt = fetchedAt;
        }
    }
}
