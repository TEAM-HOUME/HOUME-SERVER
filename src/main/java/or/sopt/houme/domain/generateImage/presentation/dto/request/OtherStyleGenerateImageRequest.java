package or.sopt.houme.domain.generateImage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OtherStyleGenerateImageRequest(
        @NotNull(message = "bannerId는 필수입니다.")
        Long bannerId,
        @NotNull(message = "floorPlanId는 필수입니다.")
        Long floorPlanId,
        @NotBlank(message = "floorPlanView는 필수입니다.")
        String floorPlanView,
        @NotNull(message = "isMirror는 필수입니다.")
        Boolean isMirror
) {
}
