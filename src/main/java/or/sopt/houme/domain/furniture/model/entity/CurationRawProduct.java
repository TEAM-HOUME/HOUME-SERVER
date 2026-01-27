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
    private Long id;

    @Column(name = "source", nullable = false)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private SoozipCategory category;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_image_url", columnDefinition = "varchar(2048)", nullable = false)
    private String productImageUrl;

    @Column(name = "product_site_url", columnDefinition = "varchar(2048)", nullable = false)
    private String productSiteUrl;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_mall_name")
    private String productMallName;

    @Column(name = "list_price")
    private Long listPrice;

    @Column(name = "discount_rate")
    private Integer discountRate;

    @Column(name = "discount_price")
    private Long discountPrice;

    @Column(name = "fetched_at", nullable = false)
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
