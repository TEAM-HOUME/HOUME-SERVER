package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request;

import jakarta.validation.constraints.NotBlank;

public record AdminBannerImageUploadRequest(
        @NotBlank String imageExtension
) {
}
