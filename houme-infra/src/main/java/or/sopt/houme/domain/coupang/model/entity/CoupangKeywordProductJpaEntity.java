package or.sopt.houme.domain.coupang.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "coupang_keyword_products",
        uniqueConstraints = @UniqueConstraint(name = "uk_coupang_keyword_product", columnNames = {"keyword_id", "product_id"})
)
@Comment("쿠팡 검색 키워드와 상품의 다대다 캐시 매핑")
public class CoupangKeywordProductJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private CoupangKeywordJpaEntity keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private CoupangProductJpaEntity product;

    private CoupangKeywordProductJpaEntity(CoupangKeywordJpaEntity keyword, CoupangProductJpaEntity product) {
        this.keyword = keyword;
        this.product = product;
    }

    public static CoupangKeywordProductJpaEntity of(CoupangKeywordJpaEntity keyword, CoupangProductJpaEntity product) {
        return new CoupangKeywordProductJpaEntity(keyword, product);
    }
}
