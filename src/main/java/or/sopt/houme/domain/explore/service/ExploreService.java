package or.sopt.houme.domain.explore.service;

import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.RecentFloorPlanResponse;
import or.sopt.houme.domain.user.model.entity.User;

public interface ExploreService {
    BannerExploreListResponse getExploreBanners(Long bannerId);

    BannerDetailResponse getExploreBannerDetail(Long bannerId);

    OtherStyleListResponse getOtherStyles(Integer size);

    OtherStyleDetailResponse getOtherStyleDetail(Long styleId);

    RecentFloorPlanResponse getRecentFloorPlan(User user);
}
