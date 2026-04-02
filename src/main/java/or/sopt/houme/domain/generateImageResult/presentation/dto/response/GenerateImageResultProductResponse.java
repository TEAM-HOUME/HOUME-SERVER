package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;

public record GenerateImageResultProductResponse(
        Long id,
        String name,
        String imageUrl,
        Long originalPrice,
        Integer discountRate,
        Long finalPrice,
        String linkUrl
) {

    public static GenerateImageResultProductResponse from(CurationRawProduct product) {
        return new GenerateImageResultProductResponse(
                product.getId(),
                product.getProductName(),
                product.getProductImageUrl(),
                product.getListPrice(),
                product.getDiscountRate(),
                product.getDiscountPrice(),
                product.getProductSiteUrl()
        );
    }
}
