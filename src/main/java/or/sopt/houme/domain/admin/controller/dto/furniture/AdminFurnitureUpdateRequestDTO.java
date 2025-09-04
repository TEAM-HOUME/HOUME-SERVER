package or.sopt.houme.domain.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureUpdateRequestDTO(
        @Schema(description = "업데이트할 가구의 한글 이름(식별자)")
        String furnitureNameKr,

        @Schema(description = "업데이트할 가구의 태그 ID(식별자)")
        Long tagId,

        @Schema(description = "새로운 가구 영어 이름")
        String newFurnitureNameEng,

        @Schema(description = "새로운 프롬프트")
        String newPrompt
) {
}
