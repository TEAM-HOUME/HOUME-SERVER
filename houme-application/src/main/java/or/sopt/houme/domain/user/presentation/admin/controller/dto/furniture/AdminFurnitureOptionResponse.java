package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureOptionResponse(
        @Schema(description = "가구 ID")
        Long furnitureId,
        @Schema(description = "가구 한글 이름")
        String furnitureNameKr,
        @Schema(description = "가구 영어 이름")
        String furnitureNameEng
) {
}
