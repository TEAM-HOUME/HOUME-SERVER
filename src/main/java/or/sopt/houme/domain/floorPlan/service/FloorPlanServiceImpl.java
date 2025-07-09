package or.sopt.houme.domain.floorPlan.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanResponse;
import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.floorPlan.repository.FloorPlanRepository;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FloorPlanServiceImpl implements FloorPlanService {

    private final FloorPlanRepository floorPlanRepository;

    // 집 구조 도면 제공 서비스 (조건에 받아서)
    @Override
    public List<FloorPlanResponse> getHousingPlan(Form form, Structure structure, Equilibrium equilibrium) {

        List<FloorPlan> allByStructureAndType =
                floorPlanRepository.findAllByStructureAndType(form, structure);

        return allByStructureAndType.stream()
                .map(FloorPlanResponse::of)
                .toList();
    }
}
