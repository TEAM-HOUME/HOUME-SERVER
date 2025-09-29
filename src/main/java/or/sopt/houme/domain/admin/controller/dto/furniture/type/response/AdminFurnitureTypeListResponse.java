package or.sopt.houme.domain.admin.controller.dto.furniture.type.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminFurnitureTypeListResponse(
        @Schema(description = "전체 가구 타입")
        List<AdminFurnitureTypeResponse> furnitureTypeList
) {

}
