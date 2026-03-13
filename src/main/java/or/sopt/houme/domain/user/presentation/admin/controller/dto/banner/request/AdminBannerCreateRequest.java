package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminBannerCreateRequest(
        @NotBlank String bannerImageUrl,
        @NotBlank String bannerTitle,
        @NotBlank String styleQuestion,
        @NotBlank String stylePrompt,
        @NotNull @Size(max = 4) List<@Valid AdminBannerStyleAnswerChipRequest> styleAnswerChips,
        @NotNull List<Long> mappedRawProductIds
) {
}
