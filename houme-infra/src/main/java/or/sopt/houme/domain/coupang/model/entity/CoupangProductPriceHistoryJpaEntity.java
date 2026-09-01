package or.sopt.houme.domain.coupang.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.global.entity.BaseEntity;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupang_product_price_histories")
@Comment("쿠팡 상품 가격 변동 이력")
public class CoupangProductPriceHistoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private CoupangProductJpaEntity product;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    private CoupangProductPriceHistoryJpaEntity(CoupangProductJpaEntity product, BigDecimal price) {
        this.product = product;
        this.price = price;
    }

    public static CoupangProductPriceHistoryJpaEntity of(CoupangProductJpaEntity product, BigDecimal price) {
        return new CoupangProductPriceHistoryJpaEntity(product, price);
    }
}
