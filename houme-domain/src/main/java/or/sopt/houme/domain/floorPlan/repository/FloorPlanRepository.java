package or.sopt.houme.domain.floorPlan.repository;

import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FloorPlanRepository extends JpaRepository<FloorPlan, Long>, FloorPlanCustomRepository {

}
