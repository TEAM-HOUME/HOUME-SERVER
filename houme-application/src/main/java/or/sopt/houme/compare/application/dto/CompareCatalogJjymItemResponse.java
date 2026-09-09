package or.sopt.houme.compare.application.dto;

import or.sopt.houme.compare.domain.EbayProduct;

public record CompareCatalogJjymItemResponse(
        Long catalogItemId,
        String title,
        String imageUrl,
        double priceUsd,
        String productUrl
) {
    public static CompareCatalogJjymItemResponse from(EbayProduct item) {
        return new CompareCatalogJjymItemResponse(
                item.id(), item.title(), item.imageUrl(), item.priceUsd(), item.productUrl()
        );
    }
}
