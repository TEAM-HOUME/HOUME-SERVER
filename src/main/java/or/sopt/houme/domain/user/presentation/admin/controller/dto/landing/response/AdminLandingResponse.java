package or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response;

import java.time.LocalDateTime;

public record AdminLandingResponse(
        Long id,
        String bannerImageUrl,
        String bannerTitle,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
