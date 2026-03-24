package or.sopt.houme.domain.explore.service;

import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerDetailResponse;

public interface ExploreService {
    BannerExploreListResponse getExploreBanners(Long bannerId);

    BannerDetailResponse getExploreBannerDetail(Long bannerId);
}
