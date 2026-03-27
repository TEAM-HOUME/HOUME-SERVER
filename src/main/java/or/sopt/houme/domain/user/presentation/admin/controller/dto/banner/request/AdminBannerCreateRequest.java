package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminBannerCreateRequest(
        @NotBlank String bannerImageUrl,
        @NotBlank String landingImageUrl,
        @NotBlank String bannerTitle,
        @NotBlank String styleDescription,
        @NotBlank String styleQuestion,
        @NotBlank String stylePrompt,
        @NotNull @Size(max = 4) List<@NotNull @Valid AdminBannerStyleAnswerChipRequest> styleAnswerChips,
        @NotNull List<@NotNull @Positive Long> mappedRawProductIds
) {
}
