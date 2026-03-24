package or.sopt.houme.domain.generateImage.model.entity;

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
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "generate_image_used_products")
@Comment("생성 이미지와 추천 가구(raw product) 매핑 테이블")
public class GenerateImageUsedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generate_image_id", nullable = false)
    private GenerateImage generateImage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curation_raw_product_id", nullable = false)
    private CurationRawProduct curationRawProduct;

    @Comment("노출 순서")
    @jakarta.persistence.Column(name = "sort_order")
    private Integer sortOrder;

    public static GenerateImageUsedProduct of(
            GenerateImage generateImage,
            CurationRawProduct curationRawProduct,
            Integer sortOrder
    ) {
        return GenerateImageUsedProduct.builder()
                .generateImage(generateImage)
                .curationRawProduct(curationRawProduct)
                .sortOrder(sortOrder)
                .build();
    }
}
