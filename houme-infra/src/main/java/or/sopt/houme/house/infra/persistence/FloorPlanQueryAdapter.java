package or.sopt.houme.house.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.house.domain.port.out.FloorPlanQueryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** {@link FloorPlanQueryPort} 의 JPA 구현 어댑터. */
@Component
@RequiredArgsConstructor
public class FloorPlanQueryAdapter implements FloorPlanQueryPort {

    private final FloorPlanRepository floorPlanRepository;

    @Override
    public boolean existsById(Long floorPlanId) {
        return floorPlanRepository.existsById(floorPlanId);
    }

    @Override
    public Optional<Long> findFirstIdByCondition(Form form, Structure structure, Equilibrium equilibrium) {
        return floorPlanRepository.findFirstByFormAndStructureAndEquilibrium(form, structure, equilibrium)
                .map(FloorPlan::getId);
    }
}
