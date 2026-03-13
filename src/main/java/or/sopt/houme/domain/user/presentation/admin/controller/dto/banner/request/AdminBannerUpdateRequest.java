package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AdminBannerUpdateRequest(
        String bannerImageUrl,
        String bannerTitle,
        String styleQuestion,
        String stylePrompt,
        @Size(max = 4) List<@Valid AdminBannerStyleAnswerChipRequest> styleAnswerChips,
        List<Long> mappedRawProductIds,
        Boolean isExposed
) {
}
