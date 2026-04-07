package or.sopt.houme.domain.house.repository.floorPlan;

import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FloorPlanRepository extends JpaRepository<FloorPlan, Long>, FloorPlanCustomRepository {
    Optional<FloorPlan> findFirstByFormAndStructureAndEquilibrium(
            Form form,
            Structure structure,
            Equilibrium equilibrium
    );
}
