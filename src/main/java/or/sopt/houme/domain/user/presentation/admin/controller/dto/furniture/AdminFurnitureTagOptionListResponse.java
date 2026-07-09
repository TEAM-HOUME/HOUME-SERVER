package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminFurnitureTagOptionListResponse(
        @Schema(description = "선택한 가구 타입에 연결된 가구 태그 목록")
        List<AdminFurnitureTagOptionResponse> furnitureTags
) {
}
