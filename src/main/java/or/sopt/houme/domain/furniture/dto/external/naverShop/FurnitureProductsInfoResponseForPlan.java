package or.sopt.houme.domain.furniture.dto.external.naverShop;

import java.util.List;

public record FurnitureProductsInfoResponseForPlan(
        String userName,
        List<FurnitureProductsInfoResponseForPlan.FurnitureProductInfo> products
) {
    public static FurnitureProductsInfoResponseForPlan of(String userName, List<FurnitureProductsInfoResponseForPlan.FurnitureProductInfo> products) {
        return new FurnitureProductsInfoResponseForPlan(userName, products);
    }

    public record FurnitureProductInfo(
            Double similarity,
            String furnitureProductImageUrl,
            String furnitureProductSiteUrl,
            String furnitureProductName,
            String furnitureProductMallName,
            String furnitureProductLprice,
            String furnitureProductId,
            String furnitureProductBrand,
            String furnitureProductMaker
    ) {
        public static FurnitureProductsInfoResponseForPlan.FurnitureProductInfo of(
                Double similarity,
                String furnitureProductImageUrl,
                String furnitureProductSiteUrl,
                String furnitureProductName,
                String furnitureProductMallName,
                String furnitureProductLprice,
                String furnitureProductId,
                String furnitureProductBrand,
                String furnitureProductMaker
        ) {
            return new FurnitureProductsInfoResponseForPlan.FurnitureProductInfo(
                    similarity,
                    furnitureProductImageUrl,
                    furnitureProductSiteUrl,
                    furnitureProductName,
                    furnitureProductMallName,
                    furnitureProductLprice,
                    furnitureProductId,
                    furnitureProductBrand,
                    furnitureProductMaker
            );
        }
    }
}
