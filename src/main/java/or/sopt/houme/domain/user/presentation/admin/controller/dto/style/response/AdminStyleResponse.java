package or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response;

import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerMappedRawProductResponse;

import java.time.LocalDateTime;
import java.util.List;

public record AdminStyleResponse(
        Long id,
        String bannerImageUrl,
        String bannerTitle,
        String styleDescription,
        String stylePrompt,
        List<AdminBannerMappedRawProductResponse> mappedRawProducts,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
