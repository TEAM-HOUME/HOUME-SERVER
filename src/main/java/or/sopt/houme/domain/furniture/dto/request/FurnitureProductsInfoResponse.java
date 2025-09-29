package or.sopt.houme.domain.furniture.dto.request;

import java.util.List;

public record FurnitureProductsInfoResponse(
        String userName,
        List<FurnitureProductInfo> products
) {
    public static FurnitureProductsInfoResponse of(String userName, List<FurnitureProductInfo> products) {
        return new FurnitureProductsInfoResponse(userName, products);
    }

    public record FurnitureProductInfo(
            String furnitureProductImageUrl,
            String furnitureProductSiteUrl,
            String furnitureProductName,
            String furnitureProductBrandName
    ) {
        public static FurnitureProductInfo of(
                String furnitureProductImageUrl,
                String furnitureProductSiteUrl,
                String furnitureProductName,
                String furnitureProductBrandName
        ) {
            return new FurnitureProductInfo(
                    furnitureProductImageUrl,
                    furnitureProductSiteUrl,
                    furnitureProductName,
                    furnitureProductBrandName
            );
        }
    }
}
