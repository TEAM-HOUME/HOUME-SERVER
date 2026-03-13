package or.sopt.houme.domain.banner.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "banner_curation_raw_products",
        indexes = {
                @Index(name = "idx_banner_raw_product_banner_id", columnList = "banner_id"),
                @Index(name = "idx_banner_raw_product_raw_product_id", columnList = "curation_raw_product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_banner_raw_product",
                        columnNames = {"banner_id", "curation_raw_product_id"}
                )
        }
)
@Comment("배너와 원본 상품의 매핑 테이블")
public class BannerCurationRawProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banner_id", nullable = false)
    private Banner banner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curation_raw_product_id", nullable = false)
    private CurationRawProduct curationRawProduct;

    public static BannerCurationRawProduct of(Banner banner, CurationRawProduct curationRawProduct) {
        return BannerCurationRawProduct.builder()
                .banner(banner)
                .curationRawProduct(curationRawProduct)
                .build();
    }
}
