package or.sopt.houme.domain.explore.presentation.dto.response;

import java.util.List;

public record BannerExploreListResponse(
        List<BannerExploreResponse> banners
) {

    public static BannerExploreListResponse of(List<BannerExploreResponse> banners) {
        return new BannerExploreListResponse(banners);
    }
}
