package or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response;

import java.time.LocalDateTime;
import java.util.List;

public record AdminBannerResponse(
        Long id,
        String bannerImageUrl,
        String landingImageUrl,
        String bannerTitle,
        String styleDescription,
        String styleQuestion,
        String stylePrompt,
        List<AdminBannerStyleAnswerChipResponse> styleAnswerChips,
        List<AdminBannerMappedRawProductResponse> mappedRawProducts,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
