package or.sopt.houme.domain.furniture.infrastructure.dto.external.naverShop;

import java.util.List;

public record FurnitureProductsInfoResponse(
        String userName,
        List<FurnitureProductInfo> products
) {
    public static FurnitureProductsInfoResponse of(String userName, List<FurnitureProductInfo> products) {
        return new FurnitureProductsInfoResponse(userName, products);
    }

    public record FurnitureProductInfo(
            Long id,
            String furnitureProductImageUrl,
            String furnitureProductSiteUrl,
            String furnitureProductName,
            String furnitureProductMallName,
            Long furnitureProductId,
            double similarity,
            List<String> colors,
            Long listPrice,
            Integer discountRate,
            Long discountPrice,
            String brandName,
            Long jjymCount
    ) {
        public static FurnitureProductInfo of(
                Long id,
                String furnitureProductImageUrl,
                String furnitureProductSiteUrl,
                String furnitureProductName,
                String furnitureProductMallName,
                Long furnitureProductId,
                double similarity
        ) {
            return new FurnitureProductInfo(
                    id,
                    furnitureProductImageUrl,
                    furnitureProductSiteUrl,
                    furnitureProductName,
                    furnitureProductMallName,
                    furnitureProductId,
                    similarity,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    0L
            );
        }

        public static FurnitureProductInfo of(
                Long id,
                String furnitureProductImageUrl,
                String furnitureProductSiteUrl,
                String furnitureProductName,
                String furnitureProductMallName,
                Long furnitureProductId,
                double similarity,
                List<String> colors,
                Long listPrice,
                Integer discountRate,
                Long discountPrice,
                String brandName,
                Long jjymCount
        ) {
            return new FurnitureProductInfo(
                    id,
                    furnitureProductImageUrl,
                    furnitureProductSiteUrl,
                    furnitureProductName,
                    furnitureProductMallName,
                    furnitureProductId,
                    similarity,
                    colors,
                    listPrice,
                    discountRate,
                    discountPrice,
                    brandName,
                    jjymCount
            );
        }
    }
}
