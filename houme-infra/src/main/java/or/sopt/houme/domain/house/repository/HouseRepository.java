package or.sopt.houme.domain.house.repository;

import or.sopt.houme.house.infra.persistence.HouseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseRepository extends JpaRepository<HouseJpaEntity, Long>, HouseCustomRepository {
    java.util.List<HouseJpaEntity> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE HouseJpaEntity h SET h.banner = null WHERE h.banner.id = :bannerId")
    int clearBannerReference(@Param("bannerId") Long bannerId);
}
