package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record FurnitureProductsInfoResponseV2(
        String userName,
        List<ProductWrapper> products
) {
    public static FurnitureProductsInfoResponseV2 of(String userName, List<ProductWrapper> products) {
        return new FurnitureProductsInfoResponseV2(userName, products);
    }

    public record ProductWrapper(
            ProductInfo product
    ) {
        public static ProductWrapper of(ProductInfo product) {
            return new ProductWrapper(product);
        }
    }

    public record ProductInfo(
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
            String linkUrl,
            List<ProductColorResponse> colors,
            boolean isLiked
    ) {
    }
}
