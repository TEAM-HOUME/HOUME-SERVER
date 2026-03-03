package or.sopt.houme.domain.furniture.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;

import java.time.LocalDateTime;

public record AdminCurationRawProductCreateRequest(
        @NotBlank(message = "source는 필수 입력값입니다.")
        String source,

        @NotNull(message = "category는 필수 입력값입니다.")
        SoozipCategory category,

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
        Long listPrice,
        Integer discountRate,
        Long discountPrice,
        Long baseShippingFee,
        Long freeShippingCondition,
        LocalDateTime fetchedAt
) {
}
