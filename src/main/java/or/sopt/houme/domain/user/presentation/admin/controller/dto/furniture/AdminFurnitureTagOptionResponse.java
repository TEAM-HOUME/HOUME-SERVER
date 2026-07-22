package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureTagOptionResponse(
        @Schema(description = "가구 태그 식별자")
        Long furnitureTagId,

        @Schema(description = "가구 식별자")
        Long furnitureId,

        @Schema(description = "가구 한글 이름")
        String furnitureNameKr,

        @Schema(description = "가구 타입 식별자")
        Long furnitureTypeId,

        @Schema(description = "가구 타입 한글 이름")
        String furnitureTypeNameKr,

        @Schema(description = "스타일 태그 식별자")
        Long tagId,

        @Schema(description = "스타일 태그 한글 이름")
        String tagNameKr,

        @Schema(description = "검색 키워드")
        String searchKeyword,

        @Schema(description = "우선순위")
        Integer priority
) {
}
