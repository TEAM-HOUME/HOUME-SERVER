package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminBannerStyleAnswerChipRequest(
        @NotNull Integer order,
        @NotBlank String label,
        @NotNull Long curationRawProductId
) {
}
