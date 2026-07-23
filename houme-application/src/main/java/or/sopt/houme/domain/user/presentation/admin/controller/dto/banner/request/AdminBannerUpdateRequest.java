package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminBannerUpdateRequest(
        String bannerImageUrl,
        String bannerTitle,
        String styleDescription,
        String styleQuestion,
        String stylePrompt,
        @Size(max = 4) List<@NotNull @Valid AdminBannerStyleAnswerChipRequest> styleAnswerChips,
        List<@NotNull @Positive Long> mappedRawProductIds
) {
}
