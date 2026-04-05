package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record CurationProductDetailResponse(ProductDetail product) {
    public record ProductDetail(
            Long productId,
            String categoryName,
            String source,
            String brand,
            String name,
            String imageUrl,
            Long originalPrice,
            Integer discountRate,
            Long finalPrice,
            String mallName,
            String linkUrl,
            List<ProductColorDetail> colors,
            Boolean isLiked
    ) {}

    public record ProductColorDetail(
            String name,
            String value
    ) {}
}
