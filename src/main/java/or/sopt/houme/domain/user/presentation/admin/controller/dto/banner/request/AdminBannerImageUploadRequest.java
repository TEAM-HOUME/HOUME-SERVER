package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminBannerImageUploadRequest(
        @NotBlank
        @Pattern(
                regexp = "^(?i)(jpg|jpeg|png|gif|webp)$",
                message = "지원하지 않는 이미지 확장자입니다."
        )
        String imageExtension
) {
}
