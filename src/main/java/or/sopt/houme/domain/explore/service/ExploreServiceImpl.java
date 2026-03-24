package or.sopt.houme.domain.explore.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExploreServiceImpl implements ExploreService {

    private final BannerRepository bannerRepository;

    @Override
    public BannerExploreListResponse getExploreBanners(Long bannerId) {
        List<Banner> banners = bannerRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        int startIndex = findBannerStartIndex(banners, bannerId);

        List<BannerExploreResponse> orderedBanners = new ArrayList<>(banners.size());
        for (int index = 0; index < banners.size(); index++) {
            Banner banner = banners.get((startIndex + index) % banners.size());
            orderedBanners.add(BannerExploreResponse.from(banner));
        }
        return BannerExploreListResponse.of(orderedBanners);
    }

    private int findBannerStartIndex(List<Banner> banners, Long bannerId) {
        for (int index = 0; index < banners.size(); index++) {
            if (banners.get(index).getId().equals(bannerId)) {
                return index;
            }
        }
        throw new GeneralException(ErrorCode.NOT_FOUND_BANNER);
    }
}
