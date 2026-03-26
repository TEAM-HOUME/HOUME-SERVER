package or.sopt.houme.domain.banner.presentation.dto.response;

import or.sopt.houme.domain.banner.model.entity.Banner;

public record BannerExploreResponse(
        Long id,
        String name,
        String imageUrl
) {

    public static BannerExploreResponse from(Banner banner) {
        return new BannerExploreResponse(
                banner.getId(),
                banner.getBannerTitle(),
                banner.getBannerImageUrl()
        );
    }
}
