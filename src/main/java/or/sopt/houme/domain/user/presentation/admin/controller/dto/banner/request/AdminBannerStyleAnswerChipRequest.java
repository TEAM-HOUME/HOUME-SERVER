package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdminBannerStyleAnswerChipRequest(
        @Min(1)
        @Max(4)
        @NotNull Integer order,
        @NotBlank String label,
        @NotNull @Positive Long curationRawProductId
) {
}
