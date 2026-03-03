package or.sopt.houme.domain.furniture.presentation.dto.request;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;

import java.time.LocalDateTime;

public record AdminCurationRawProductUpdateRequest(
        String source,
        SoozipCategory category,
        Long productId,
        String productImageUrl,
        String productSiteUrl,
        String productName,
        String productMallName,
        String brand,
        Long listPrice,
        Integer discountRate,
        Long discountPrice,
        Long baseShippingFee,
        Long freeShippingCondition,
        LocalDateTime fetchedAt
) {
}
