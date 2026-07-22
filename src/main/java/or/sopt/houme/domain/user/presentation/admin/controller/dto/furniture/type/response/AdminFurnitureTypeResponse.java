package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.type.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureTypeResponse(
        @Schema(description = "가구 타입 식별자")
        Long furnitureTypeId,   // 가구 타입 식별자
        @Schema(description = "가구 타입 한글명")
        String furnitureTypeNameKr, // 가구 타입 한글명
        @Schema(description = "가구 타입 영어명")
        String furnitureTypeNameEng // 가구 타입 영어명
) {
}
