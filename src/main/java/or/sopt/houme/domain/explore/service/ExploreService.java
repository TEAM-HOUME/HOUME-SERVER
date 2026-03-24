package or.sopt.houme.domain.explore.service;

import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.OtherStyleDetailResponse;

public interface ExploreService {
    BannerExploreListResponse getExploreBanners(Long bannerId);

    BannerDetailResponse getExploreBannerDetail(Long bannerId);

    OtherStyleListResponse getOtherStyles(Integer size);

    OtherStyleDetailResponse getOtherStyleDetail(Long styleId);
}
