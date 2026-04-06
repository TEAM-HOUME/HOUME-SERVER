package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.presentation.dto.response.ProductColorResponse;

import java.util.List;

public record GenerateImageResultProductResponse(
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

    public static GenerateImageResultProductResponse from(
            CurationRawProduct product,
            List<ProductColorResponse> colors,
            boolean isLiked
    ) {
        return new GenerateImageResultProductResponse(
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
