package or.sopt.houme.domain.floorPlan.repository;

import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface FloorPlanRepository extends JpaRepository<FloorPlan, Long> {
}
