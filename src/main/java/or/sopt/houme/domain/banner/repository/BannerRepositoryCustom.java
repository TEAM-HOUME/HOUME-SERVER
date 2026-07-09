package or.sopt.houme.domain.banner.repository;

import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;

import java.util.List;
import java.util.Optional;

public interface BannerRepositoryCustom {

    Optional<Banner> findByIdWithRawProducts(Long bannerId, BannerType bannerType, boolean includeLegacyBanner);

    List<Banner> findAllWithRawProducts(BannerType bannerType, boolean includeLegacyBanner);

    List<Banner> findAllByIdInWithRawProducts(List<Long> bannerIds);

    List<Banner> findAllLandingsWithLinkedBanner();
}
