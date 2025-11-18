package or.sopt.houme.domain.admin.controller.dto.furniture;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record AdminFurnitureTagGetDTO(

        @Schema(description = "tag 식별자 입니다")
        List<Long> tagId,

        @Schema(description = "tag의 한글 이름 입니다")
        List<String> tagNameKr

) {
}
