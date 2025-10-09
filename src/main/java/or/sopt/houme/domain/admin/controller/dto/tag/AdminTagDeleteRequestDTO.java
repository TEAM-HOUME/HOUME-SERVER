package or.sopt.houme.domain.admin.controller.dto.tag;

import jakarta.validation.constraints.NotNull;

public record AdminTagDeleteRequestDTO(

        @NotNull(message = "태그 ID는 필수입니다.")
        Long tagId

) {
}
