package or.sopt.houme.domain.furniture.model.entity;

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
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(
        name = "curation_raw_product_furniture_tags",
        indexes = {
                @Index(name = "idx_raw_product_furniture_tag_raw_product_id", columnList = "curation_raw_product_id"),
                @Index(name = "idx_raw_product_furniture_tag_furniture_tag_id", columnList = "furniture_tag_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_raw_product_furniture_tag",
                        columnNames = {"curation_raw_product_id", "furniture_tag_id"}
                )
        }
)
@Comment("큐레이션 원본 수집 데이터(curation_raw_products)와 가구 태그(furniture_tags)의 매핑 테이블입니다. " +
        "하나의 furniture_tag에 여러개의 가구가 매핑 될 수 있어 추가되었습니다.")
public class CurationRawProductFurnitureTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curation_raw_product_id", nullable = false)
    private CurationRawProduct curationRawProduct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "furniture_tag_id", nullable = false)
    private FurnitureTag furnitureTag;

    public void updateFurnitureTag(FurnitureTag furnitureTag) {
        this.furnitureTag = furnitureTag;
    }

    public static CurationRawProductFurnitureTag of(CurationRawProduct rawProduct, FurnitureTag furnitureTag) {
        return CurationRawProductFurnitureTag.builder()
                .curationRawProduct(rawProduct)
                .furnitureTag(furnitureTag)
                .build();
    }
}

