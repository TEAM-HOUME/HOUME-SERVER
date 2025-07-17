package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.entity.mapping.HouseFloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseFloorPlanRepository extends JpaRepository<HouseFloorPlan, Long> {

}
