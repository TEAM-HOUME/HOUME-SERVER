package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.entity.mapping.HouseFurniture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseFurnitureRepository extends JpaRepository<HouseFurniture, Long> {
    void deleteByHouseId(Long houseId);
}
