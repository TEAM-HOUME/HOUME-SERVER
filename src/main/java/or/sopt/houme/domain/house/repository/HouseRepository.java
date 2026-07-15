package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.model.entity.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseRepository extends JpaRepository<House, Long>, HouseCustomRepository {
    java.util.List<House> findByUserId(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE House h SET h.banner = null WHERE h.banner.id = :bannerId")
    int clearBannerReference(@Param("bannerId") Long bannerId);
}
