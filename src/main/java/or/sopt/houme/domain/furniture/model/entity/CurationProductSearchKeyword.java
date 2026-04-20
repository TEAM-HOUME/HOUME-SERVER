package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@Entity
@Table(
        name = "curation_product_search_keywords",
        indexes = {
                @Index(name = "idx_search_keyword_product_id", columnList = "curation_raw_product_id")
        }
)
@Comment("상품 커스텀 검색 키워드 매핑 테이블")
public class CurationProductSearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("식별자")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curation_raw_product_id", nullable = false)
    @Comment("매핑된 상품")
    private CurationRawProduct curationRawProduct;

    @Column(name = "keyword", nullable = false)
    @Comment("커스텀 검색 키워드 (상품명/브랜드와 무관하지만 이 상품을 찾아야 하는 대체 키워드)")
    private String keyword;

    public static CurationProductSearchKeyword of(CurationRawProduct product, String keyword) {
        return CurationProductSearchKeyword.builder()
                .curationRawProduct(product)
                .keyword(keyword)
                .build();
    }
}
