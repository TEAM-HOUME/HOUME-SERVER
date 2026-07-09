package or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request;

import jakarta.validation.constraints.NotBlank;

public record AdminLandingCreateRequest(
        @NotBlank String bannerImageUrl,
        @NotBlank String bannerTitle
) {
}
