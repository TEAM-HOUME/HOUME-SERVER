package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureTagDeleteDTO(

        @Schema(description = "업데이트할 가구의 한글 이름(식별자)")
        String furnitureNameKr,

        @Schema(description = "업데이트할 가구의 태그 ID(식별자)")
        Long tagId

) {


}
