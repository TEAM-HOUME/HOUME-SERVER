package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;

import java.time.LocalDateTime;
import java.util.List;

public record AdminCurationRawProductResponse(
        Long id,
        String source,
        SoozipCategory category,
        Long productId,
        String productImageUrl,
        String productSiteUrl,
        String productName,
        String productMallName,
        String brand,
        Long listPrice,
        Integer discountRate,
        Long discountPrice,
        Long baseShippingFee,
        Long freeShippingCondition,
        LocalDateTime fetchedAt,
        Boolean isExposed,
        List<AdminCurationRawProductColorResponse> colors,
        List<AdminCurationRawProductFurnitureResponse> furnitures,
        List<AdminCurationRawProductFurnitureTagResponse> furnitureTags
) {
    public static AdminCurationRawProductResponse of(
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
