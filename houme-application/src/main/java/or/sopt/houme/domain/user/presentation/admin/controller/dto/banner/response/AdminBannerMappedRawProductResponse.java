package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response;

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
}
