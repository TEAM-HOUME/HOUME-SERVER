package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture.type.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminDeleteFurnitureTypeRequest(

        @Schema(description = "삭제할 가구 타입 식별자")
        Long furnitureTypeId
) {
}
