package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureUpdateRequestDTO(
        @Schema(description = "업데이트할 가구의 한글 이름(식별자)")
        String furnitureNameKr,

        @Schema(description = "업데이트할 가구의 태그 ID(식별자)")
        Long tagId,

        @Schema(description = "새로운 가구 영어 이름")
        String newFurnitureNameEng,

        @Schema(description = "새로운 프롬프트")
        String newPrompt,

        @Schema(description = "새로운 검색 키워드")
        String newSearchKeyword,

        @Schema(description = "새로운 우선순위")
        Integer newPriority,

        @Schema(description = "이미지 업데이트 시 사용할 확장자 (예: jpg, png)")
        String imageExtension,

        @Schema(description = "이미지 업데이트 시 표시용 원본 파일명")
        String originalFilename
) {
}
