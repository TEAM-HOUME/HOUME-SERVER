package or.sopt.houme.domain.banner.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.presentation.dto.response.ProductColorResponse;

import java.util.List;

public record OtherStyleDetailProductResponse(
        Long id,
        String name,
        String imageUrl,
        Long originalPrice,
        Integer discountRate,
        Long finalPrice,
        String linkUrl,
        List<ProductColorResponse> colors,
        boolean isLiked
) {

    public static OtherStyleDetailProductResponse from(
            CurationRawProduct product,
            List<ProductColorResponse> colors,
            boolean isLiked
    ) {
        return new OtherStyleDetailProductResponse(
                product.getId(),
                product.getProductName(),
                product.getProductImageUrl(),
                product.getListPrice(),
                product.getDiscountRate(),
                product.getDiscountPrice(),
                product.getProductSiteUrl(),
                colors,
                isLiked
        );
    }
}
