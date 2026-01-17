package or.sopt.houme.domain.house.repository.floorPlan;

import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FloorPlanRepository extends JpaRepository<FloorPlan, Long>, FloorPlanCustomRepository {

}
