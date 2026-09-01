package or.sopt.houme.compare.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeminiPromptRequest(
        @NotBlank(message = "프롬프트는 필수입니다.")
        @Size(max = 8_000, message = "프롬프트는 8000자 이하여야 합니다.")
        String prompt
) {
}
