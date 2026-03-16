package or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminFloorPlanImageRequest(
        @NotBlank
        String url,
        @NotBlank
        String filename,
        @NotBlank
        String originalFilename,
        @NotBlank
        String fileExtension,
        @NotNull
        @Min(1)
        Integer sortOrder
) {
}
