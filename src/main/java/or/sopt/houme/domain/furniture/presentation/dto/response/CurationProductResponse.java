package or.sopt.houme.domain.furniture.presentation.dto.response;

public record CurationProductResponse(
        Long id,
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
        String linkUrl
) {
}
