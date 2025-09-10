package or.sopt.houme.domain.admin.controller.dto;

import jakarta.validation.constraints.Pattern;

public record AdminTagUpdateRequestDTO(

        String tagNameKr,
        Integer newPriority,

        @Pattern(regexp = "^[a-zA-Z0-9\\s]*$", message = "태그 영문명은 영문자와 숫자, 공백만 포함할 수 있습니다.")
        String newTagNameEng,
        String newTagPrompt


) {
}
