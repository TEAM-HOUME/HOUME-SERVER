package or.sopt.houme.domain.admin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminFurnitureGetDto(
        @Schema(description = "가구들의 한글 이름입니다")
        List<String> furnitureKrNames
) {
}
