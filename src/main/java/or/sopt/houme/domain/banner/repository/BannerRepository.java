package or.sopt.houme.domain.banner.repository;

import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    @Query("""
            select distinct banner
            from Banner banner
            left join fetch banner.bannerRawProducts mapping
            left join fetch mapping.curationRawProduct rawProduct
            where banner.id = :bannerId
              and (
                  banner.bannerType = :bannerType
                  or (:includeLegacyBanner = true and banner.bannerType is null)
              )
            """)
    Optional<Banner> findByIdWithRawProducts(
            @Param("bannerId") Long bannerId,
            @Param("bannerType") BannerType bannerType,
            @Param("includeLegacyBanner") boolean includeLegacyBanner
    );

    @Query("""
            select distinct banner
            from Banner banner
            left join fetch banner.bannerRawProducts mapping
            left join fetch mapping.curationRawProduct rawProduct
            where banner.bannerType = :bannerType
               or (:includeLegacyBanner = true and banner.bannerType is null)
            order by banner.id desc
            """)
    List<Banner> findAllWithRawProducts(
            @Param("bannerType") BannerType bannerType,
            @Param("includeLegacyBanner") boolean includeLegacyBanner
    );
}
