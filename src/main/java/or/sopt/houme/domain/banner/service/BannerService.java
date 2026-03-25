package or.sopt.houme.domain.banner.service;

import or.sopt.houme.domain.banner.presentation.dto.response.BannerDetailResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.LandingListResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.OtherStyleDetailResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.OtherStyleListResponse;

public interface BannerService {
    LandingListResponse getLandings();

    BannerExploreListResponse getExploreBanners(Long bannerId);

    BannerDetailResponse getExploreBannerDetail(Long bannerId);

    OtherStyleListResponse getOtherStyles(Integer size);

    OtherStyleDetailResponse getOtherStyleDetail(Long styleId);
}
