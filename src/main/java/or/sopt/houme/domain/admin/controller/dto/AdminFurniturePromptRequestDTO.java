package or.sopt.houme.domain.admin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurniturePromptRequestDTO(

        @Schema(description = "가구의 한글명 입니다")
        String furnitureNameKr,

        @Schema(description = "가구에 대한 프롬프트 입니다")
        String prompt,

        @Schema(description = "가구의 스타일 태그 입니다")
        Long tagId

) {
}
