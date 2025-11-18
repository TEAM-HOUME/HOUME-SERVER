package or.sopt.houme.domain.admin.controller.dto.furniture.type.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminUpdateFurnitureTypeRequest(
        @Schema(description = "수정할 가구 타입 식별자")
        Long id,
        @Schema(description = "새로운 가구 타입 한글명")
        String furnitureTypeNameKr,
        @Schema(description = "새로운 가구 타입 영어명")
        String furnitureTypeNameEng
) {
}
