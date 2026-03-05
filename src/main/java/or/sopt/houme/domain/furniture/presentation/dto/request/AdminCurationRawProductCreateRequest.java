package or.sopt.houme.domain.furniture.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;

import java.time.LocalDateTime;

public record AdminCurationRawProductCreateRequest(
        @NotBlank(message = "source는 필수 입력값입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9_-]{0,49}$", message = "source 형식이 올바르지 않습니다.")
        String source,

        @NotNull(message = "category는 필수 입력값입니다.")
        SoozipCategory category,

        @Positive(message = "productId는 1 이상이어야 합니다.")
        @NotNull(message = "productId는 필수 입력값입니다.")
        Long productId,

        @NotBlank(message = "productImageUrl은 필수 입력값입니다.")
        String productImageUrl,

        @NotBlank(message = "productSiteUrl은 필수 입력값입니다.")
        String productSiteUrl,

        @NotBlank(message = "productName은 필수 입력값입니다.")
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
        LocalDateTime fetchedAt
) {
}
