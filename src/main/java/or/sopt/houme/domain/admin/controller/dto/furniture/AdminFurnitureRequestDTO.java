package or.sopt.houme.domain.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureRequestDTO(

        @Schema(description = "추가되는 가구의 한글명 입니다")
        String furnitureNameKr,

        @Schema(description = "추가되는 가구의 영어명 입니다")
        String furnitureNameEng,

        @Schema(description = "추가되는 가구의 가구 타입 입니다")
        Long furnitureType
) {
}
