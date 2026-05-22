package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "curation_raw_product_furnitures")
@Comment("큐레이션 원본 수집 데이터(curation_raw_products)와 하위 가구(furnitures)의 매핑 테이블입니다.")
public class CurationRawProductFurniture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curation_raw_product_id", nullable = false)
    private CurationRawProduct curationRawProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "furniture_id", nullable = false)
    private Furniture furniture;

    public static CurationRawProductFurniture of(CurationRawProduct rawProduct, Furniture furniture) {
        return CurationRawProductFurniture.builder()
                .curationRawProduct(rawProduct)
                .furniture(furniture)
                .build();
    }
}
