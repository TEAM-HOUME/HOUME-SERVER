package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;

public record SimilarItemResponse(
        Long id,
        String brand,
        String name,
        String imageUrl,
        Long originalPrice,
        Integer discountRate,
        Long finalPrice,
        String linkUrl
) {

    public static SimilarItemResponse from(CurationRawProduct product) {
        return new SimilarItemResponse(
                product.getId(),
                product.getBrand(),
                product.getProductName(),
                product.getProductImageUrl(),
                product.getListPrice(),
                product.getDiscountRate(),
                product.getDiscountPrice(),
                product.getProductSiteUrl()
        );
    }
}
