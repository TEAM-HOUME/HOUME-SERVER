package or.sopt.houme.compare.infra.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(
        name = "ebay_products",
        uniqueConstraints = @UniqueConstraint(name = "uk_ebay_products_item_id", columnNames = "ebay_item_id"),
        indexes = @Index(name = "idx_ebay_products_soozip_category", columnList = "soozip_category")
)
public class EbayProductJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ebay_item_id", nullable = false, length = 100)
    @Comment("eBay 상품 식별자")
    private String ebayItemId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    @Comment("eBay 상품명")
    private String title;

    @Column(name = "image_url", length = 2048)
    @Comment("썸네일 이미지 URL")
    private String imageUrl;

    @Column(name = "price_usd")
    @Comment("eBay 가격 (USD)")
    private Double priceUsd;

    @Column(name = "product_url", length = 2048)
    @Comment("eBay 상품 URL")
    private String productUrl;

    @Column(name = "soozip_category", length = 30)
    @Comment("수집 시 원본 상품의 수집 카테고리 (nullable)")
    private String soozipCategory;

    // ponytail: TEXT column for future ALTER TO vector(512) when pgvector search is added
    @Column(name = "title_embedding", columnDefinition = "TEXT")
    @Comment("제목 임베딩 벡터 (pgvector 문자열 형식)")
    private String titleEmbedding;

    @Column(name = "image_embedding", columnDefinition = "TEXT")
    @Comment("이미지 임베딩 벡터 (pgvector 문자열 형식)")
    private String imageEmbedding;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public EbayProductJpaEntity withId(Long id) {
        return EbayProductJpaEntity.builder()
                .id(id)
                .ebayItemId(this.ebayItemId)
                .title(this.title)
                .imageUrl(this.imageUrl)
                .priceUsd(this.priceUsd)
                .productUrl(this.productUrl)
                .soozipCategory(this.soozipCategory)
                .titleEmbedding(this.titleEmbedding)
                .imageEmbedding(this.imageEmbedding)
                .build();
    }
}
