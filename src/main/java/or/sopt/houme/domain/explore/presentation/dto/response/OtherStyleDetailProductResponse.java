package or.sopt.houme.domain.explore.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;

public record OtherStyleDetailProductResponse(
        Long id,
        String name,
        String imageUrl,
        Long originalPrice,
        Integer discountRate,
        Long finalPrice,
        String linkUrl
) {

    public static OtherStyleDetailProductResponse from(CurationRawProduct product) {
        return new OtherStyleDetailProductResponse(
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
