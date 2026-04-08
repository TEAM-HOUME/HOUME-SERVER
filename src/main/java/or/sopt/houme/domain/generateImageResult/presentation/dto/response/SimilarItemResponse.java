package or.sopt.houme.domain.generateImageResult.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.presentation.dto.response.ProductColorResponse;

import java.util.List;

public record SimilarItemResponse(
        Long id,
        String brand,
        String name,
        String imageUrl,
        Long originalPrice,
        Integer discountRate,
        Long finalPrice,
        String linkUrl,
        List<ProductColorResponse> colors,
        boolean isLiked
) {

    public static SimilarItemResponse from(
            CurationRawProduct product,
            List<ProductColorResponse> colors,
            boolean isLiked
    ) {
        return new SimilarItemResponse(
                product.getId(),
                product.getBrand(),
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
