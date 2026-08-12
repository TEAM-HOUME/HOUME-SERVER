package or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request;

import jakarta.validation.constraints.NotBlank;

public record AdminFloorPlanImageUploadRequest(
        @NotBlank
        String imageExtension
) {
}
