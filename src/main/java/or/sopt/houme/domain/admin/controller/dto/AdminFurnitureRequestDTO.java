package or.sopt.houme.domain.admin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureRequestDTO(

        @Schema(description = "추가되는 가구의 한글명 입니다")
        String furnitureNameKr,

        @Schema(description = "추가되는 가구의 영어명 입니다")
        String furnitureNameEng,

        @Schema(description = "침대인지 아닌지에 대한 필드입니다")
        boolean isBed
) {
}
