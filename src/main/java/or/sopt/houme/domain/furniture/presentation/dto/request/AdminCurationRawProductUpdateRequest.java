package or.sopt.houme.domain.furniture.presentation.dto.request;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record AdminCurationRawProductUpdateRequest(
        @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9_-]{0,49}$", message = "source 형식이 올바르지 않습니다.")
        String source,
        SoozipCategory category,
        @Positive(message = "productId는 1 이상이어야 합니다.")
        Long productId,
        @Size(max = 2048, message = "productImageUrl은 2048자 이하여야 합니다.")
        String productImageUrl,
        @Size(max = 2048, message = "productSiteUrl은 2048자 이하여야 합니다.")
        String productSiteUrl,
        String productName,
        String productMallName,
        String brand,
        @PositiveOrZero(message = "listPrice는 0 이상이어야 합니다.")
        Long listPrice,
        @Min(value = 0, message = "discountRate는 0 이상이어야 합니다.")
        @Max(value = 100, message = "discountRate는 100 이하여야 합니다.")
        Integer discountRate,
        @PositiveOrZero(message = "discountPrice는 0 이상이어야 합니다.")
        Long discountPrice,
        @PositiveOrZero(message = "baseShippingFee는 0 이상이어야 합니다.")
        Long baseShippingFee,
        @PositiveOrZero(message = "freeShippingCondition는 0 이상이어야 합니다.")
        Long freeShippingCondition,
        Boolean isExposed,
        LocalDateTime fetchedAt,
        List<AdminCurationRawProductColorRequest> colors,
        List<@Positive(message = "furnitureId는 1 이상이어야 합니다.") Long> furnitureIds,
        List<@Positive(message = "furnitureTagId는 1 이상이어야 합니다.") Long> furnitureTagIds
) {
}
