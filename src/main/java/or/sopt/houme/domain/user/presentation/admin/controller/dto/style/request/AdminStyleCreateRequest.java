package or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminStyleCreateRequest(
        @NotBlank String bannerImageUrl,
        @NotBlank String bannerTitle,
        @NotBlank String styleDescription,
        @NotBlank String stylePrompt,
        @NotNull List<Long> mappedRawProductIds
) {
}
