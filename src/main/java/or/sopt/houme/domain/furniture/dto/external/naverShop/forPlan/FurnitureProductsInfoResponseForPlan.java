package or.sopt.houme.domain.furniture.dto.external.naverShop.forPlan;

import java.util.List;

public record FurnitureProductsInfoResponseForPlan(
        String userName,
        List<FurnitureProductsInfoResponseForPlan.FurnitureProductInfo> products
) {
    public static FurnitureProductsInfoResponseForPlan of(String userName, List<FurnitureProductsInfoResponseForPlan.FurnitureProductInfo> products) {
        return new FurnitureProductsInfoResponseForPlan(userName, products);
    }

    public record FurnitureProductInfo(
            String baseFurnitureImageUrl,
            String furnitureProductImageUrl,
            String furnitureProductSiteUrl,
            String furnitureProductName,
            String furnitureProductMallName,
            String furnitureProductId,
            Double similarity
    ) {
        public static FurnitureProductsInfoResponseForPlan.FurnitureProductInfo of(
                String baseFurnitureImageUrl,
                String furnitureProductImageUrl,
                String furnitureProductSiteUrl,
                String furnitureProductName,
                String furnitureProductMallName,
                String furnitureProductId,
                Double similarity
        ) {
            return new FurnitureProductsInfoResponseForPlan.FurnitureProductInfo(
                    baseFurnitureImageUrl,
                    furnitureProductImageUrl,
                    furnitureProductSiteUrl,
                    furnitureProductName,
                    furnitureProductMallName,
                    furnitureProductId,
                    similarity
            );
        }
    }
}
