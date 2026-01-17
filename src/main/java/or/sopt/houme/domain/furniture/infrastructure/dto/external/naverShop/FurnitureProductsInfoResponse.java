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
            double similarity
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
                    similarity
            );
        }
    }
}
