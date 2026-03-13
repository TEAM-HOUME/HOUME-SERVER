package or.sopt.houme.domain.banner.repository;

import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerCurationRawProductRepository extends JpaRepository<BannerCurationRawProduct, Long> {

    @Query("""
            select mapping
            from BannerCurationRawProduct mapping
            join fetch mapping.curationRawProduct rawProduct
            where mapping.banner.id in :bannerIds
            order by mapping.id asc
            """)
    List<BannerCurationRawProduct> findAllByBannerIdInWithRawProduct(@Param("bannerIds") List<Long> bannerIds);
}
