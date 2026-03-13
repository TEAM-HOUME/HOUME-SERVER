package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;

public record AdminBannerMappedRawProductResponse(
        Long id,
        String source,
        SoozipCategory category,
        Long productId,
        String productName,
        String productImageUrl,
        String brand
) {
    public static AdminBannerMappedRawProductResponse of(CurationRawProduct rawProduct) {
        return new AdminBannerMappedRawProductResponse(
                rawProduct.getId(),
                rawProduct.getSource(),
                rawProduct.getCategory(),
                rawProduct.getProductId(),
                rawProduct.getProductName(),
                rawProduct.getProductImageUrl(),
                rawProduct.getBrand()
        );
    }
}
