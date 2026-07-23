package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureDetailsRequestDTO (

        @Schema(description = "가구의 한글명 입니다")
        String furnitureNameKr,

        @Schema(description = "조회 할 가구의 태그 ID(식별자)")
        Long tagId

){
}
