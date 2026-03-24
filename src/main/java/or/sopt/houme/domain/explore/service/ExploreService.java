package or.sopt.houme.domain.explore.service;

import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;

public interface ExploreService {
    BannerExploreListResponse getExploreBanners(Long bannerId);
}
