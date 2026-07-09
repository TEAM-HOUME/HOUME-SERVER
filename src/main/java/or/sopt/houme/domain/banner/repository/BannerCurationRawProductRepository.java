package or.sopt.houme.domain.banner.repository;

import or.sopt.houme.domain.banner.model.entity.BannerCurationRawProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerCurationRawProductRepository extends JpaRepository<BannerCurationRawProduct, Long> {
}
