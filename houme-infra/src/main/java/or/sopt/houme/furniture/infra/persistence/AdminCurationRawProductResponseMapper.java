package or.sopt.houme.furniture.infra.persistence;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductColorResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductFurnitureResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductFurnitureTagResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductResponse;

import java.util.List;

/**
 * #582: presentation DTO 를 엔티티-프리로 유지하기 위해, 엔티티 그래프→DTO 매핑을 infra 로 이관한 매퍼.
 */
public final class AdminCurationRawProductResponseMapper {

    private AdminCurationRawProductResponseMapper() {
    }

    public static AdminCurationRawProductColorResponse toColorResponse(CurationRawProductColor color) {
        return new AdminCurationRawProductColorResponse(
                color.getId(),
                color.getRawColorName(),
                color.getClientColorName()
        );
    }

    public static AdminCurationRawProductFurnitureResponse toFurnitureResponse(CurationRawProductFurniture mapping) {
        FurnitureJpaEntity furniture = mapping.getFurniture();

        return new AdminCurationRawProductFurnitureResponse(
                mapping.getId(),
                furniture != null ? furniture.getId() : null,
                furniture != null ? furniture.getFurnitureNameKr() : null,
                furniture != null ? furniture.getFurnitureNameEng() : null,
                furniture != null && furniture.getFurnitureType() != null ? furniture.getFurnitureType().getId() : null,
                furniture != null && furniture.getFurnitureType() != null ? furniture.getFurnitureType().getNameKr() : null
        );
    }

    /** @param tagNameKr furnitureTag.getTagId() 로 조회한 태그 한글명(없으면 null) */
    public static AdminCurationRawProductFurnitureTagResponse toFurnitureTagResponse(
            CurationRawProductFurnitureTag mapping, String tagNameKr) {
        FurnitureTag furnitureTag = mapping.getFurnitureTag();
        FurnitureJpaEntity furniture = furnitureTag != null ? furnitureTag.getFurniture() : null;

        return new AdminCurationRawProductFurnitureTagResponse(
                mapping.getId(),
                furnitureTag != null ? furnitureTag.getId() : null,
                furniture != null ? furniture.getId() : null,
                furniture != null ? furniture.getFurnitureNameKr() : null,
                furniture != null && furniture.getFurnitureType() != null ? furniture.getFurnitureType().getId() : null,
                furniture != null && furniture.getFurnitureType() != null ? furniture.getFurnitureType().getNameKr() : null,
                furnitureTag != null ? furnitureTag.getTagId() : null,
                tagNameKr,
                furnitureTag != null ? furnitureTag.getPriority() : null,
                furnitureTag != null ? furnitureTag.getSearchKeyword() : null
        );
    }

    public static AdminCurationRawProductResponse toResponse(
            CurationRawProduct rawProduct,
            List<AdminCurationRawProductColorResponse> colors,
            List<AdminCurationRawProductFurnitureResponse> furnitures,
            List<AdminCurationRawProductFurnitureTagResponse> furnitureTags
    ) {
        return new AdminCurationRawProductResponse(
                rawProduct.getId(),
                rawProduct.getSource(),
                rawProduct.getCategory(),
                rawProduct.getProductId(),
                rawProduct.getProductImageUrl(),
                rawProduct.getProductSiteUrl(),
                rawProduct.getProductName(),
                rawProduct.getProductMallName(),
                rawProduct.getBrand(),
                rawProduct.getListPrice(),
                rawProduct.getDiscountRate(),
                rawProduct.getDiscountPrice(),
                rawProduct.getBaseShippingFee(),
                rawProduct.getFreeShippingCondition(),
                rawProduct.getFetchedAt(),
                rawProduct.getIsExposed(),
                colors,
                furnitures,
                furnitureTags
        );
    }
}
