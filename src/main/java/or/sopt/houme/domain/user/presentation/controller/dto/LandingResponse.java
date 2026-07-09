package or.sopt.houme.domain.user.presentation.controller.dto;

import or.sopt.houme.domain.banner.model.entity.Banner;

public record LandingResponse(
        Long id,
        String name,
        String imageUrl
) {

    public static LandingResponse from(Banner banner) {
        return new LandingResponse(
                banner.getId(),
                banner.getBannerTitle(),
                banner.getBannerImageUrl()
        );
    }
}
