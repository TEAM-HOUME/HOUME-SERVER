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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupang_products")
@Comment("쿠팡 파트너스 검색 상품 캐시")
public class CoupangProductJpaEntity extends BaseEntity {

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
    }
}
