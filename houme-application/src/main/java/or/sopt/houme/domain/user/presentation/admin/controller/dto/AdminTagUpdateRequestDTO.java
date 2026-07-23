package or.sopt.houme.domain.user.presentation.admin.controller.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

public record AdminTagUpdateRequestDTO(

        @NotNull(message = "태그 ID는 필수입니다.")
        Long tagId,
        Integer newPriority,

        @Pattern(
                regexp = "^[a-zA-Z0-9\\s\\-_.()&/]*$",
                message = "태그 영문명은 영문자/숫자/공백/-,_,.,(),&,/ 만 허용됩니다."
        )
        String newTagNameEng,
        String newTagPrompt,
        String newTagNameKr


) {
}
