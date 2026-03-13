package or.sopt.houme.domain.banner.repository;

import or.sopt.houme.domain.banner.model.entity.Banner;
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
            """)
    Optional<Banner> findByIdWithRawProducts(@Param("bannerId") Long bannerId);

    @Query("""
            select distinct banner
            from Banner banner
            left join fetch banner.bannerRawProducts mapping
            left join fetch mapping.curationRawProduct rawProduct
            order by banner.id desc
            """)
    List<Banner> findAllWithRawProducts();
}
