package or.sopt.houme.domain.banner.presentation.dto.response;

import or.sopt.houme.domain.banner.model.entity.Banner;

public record LandingResponse(
        Long id,
        Long bannerId,
        String name,
        String imageUrl
) {

    public static LandingResponse from(Banner banner) {
        return new LandingResponse(
                banner.getId(),
                banner.getLinkedBanner() != null ? banner.getLinkedBanner().getId() : null,
                banner.getBannerTitle(),
                banner.getBannerImageUrl()
        );
    }
}
