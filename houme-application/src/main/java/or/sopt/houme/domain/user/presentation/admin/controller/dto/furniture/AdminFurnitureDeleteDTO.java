package or.sopt.houme.domain.user.presentation.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminFurnitureDeleteDTO(

        @Schema(description = "업데이트할 가구의 한글 이름(식별자)")
        String furnitureNameKr

) {
}
