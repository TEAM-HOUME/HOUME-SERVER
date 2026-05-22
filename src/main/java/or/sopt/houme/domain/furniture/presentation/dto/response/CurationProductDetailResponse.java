package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record CurationProductDetailResponse(ProductDetail product) {
    public record ProductDetail(
            Long id,               // 우리 DB의 내부 PK (API 요청 및 찜 연동용)
            Long productId,        // 외부 수집처의 원본 상품 식별자
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
            Boolean isLiked,
            Long jjymCount
    ) {}

    public record ProductColorDetail(
            String name,
            String value
    ) {}
}
