package or.sopt.houme.domain.furniture.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(
        name = "recommend_furnitures",
        indexes = {
                @Index(name = "idx_furniture_product_id", columnList = "furniture_product_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_furniture_product_id",
                        columnNames = {"furniture_product_id"}
                )
        }
)
public class RecommendFurniture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "furniture_product_url")
    @Comment("추천 가구 이미지 url")
    private String furnitureProductImageUrl;

    @Column(name = "furniture_product_site_url")
    @Comment("추천 가구 구매 사이트 url")
    private String furnitureProductSiteUrl;

    @Column(name = "furniture_product_name")
    @Comment("추천 가구명")
    private String furnitureProductName;

    @Column(name = "furniture_product_mall_name")
    @Comment("추천 가구 판매 회사")
    private String furnitureProductMallName;

    @Column(name = "furniture_product_id", nullable = false)
    @Comment("추천 가구 식별자")
    private Long furnitureProductId;



    public static RecommendFurniture from(
            String furnitureProductImageUrl,
            String furnitureProductSiteUrl,
            String furnitureProductName,
            String furnitureProductMallName,
            Long furnitureProductId
    ){
        return RecommendFurniture.builder()
                .furnitureProductImageUrl(furnitureProductImageUrl)
                .furnitureProductSiteUrl(furnitureProductSiteUrl)
                .furnitureProductName(furnitureProductName)
                .furnitureProductMallName(furnitureProductMallName)
                .furnitureProductId(furnitureProductId)
                .build();
    }
}
