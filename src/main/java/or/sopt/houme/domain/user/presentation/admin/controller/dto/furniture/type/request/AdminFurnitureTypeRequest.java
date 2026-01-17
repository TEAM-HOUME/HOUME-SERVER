package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.type.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureTypeRequest(
        @Schema(description = "추가할 가구 타입 한글명")
        String furnitureTypeNameKr, // 가구 타입 한글명
        @Schema(description = "추가할 가구 타입 영어명")
        String furnitureTypeNameEng // 가구 타입 영어명
) {
}
