package or.sopt.houme.domain.floorPlan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanResponse;
import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.floorPlan.repository.FloorPlanRepository;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FloorPlanServiceImpl implements FloorPlanService {

    private final FloorPlanRepository floorPlanRepository;

    // 집 구조 도면 제공 서비스 (조건에 받아서)
    @Override
    public List<FloorPlanResponse> getHousingPlan(Form form, Structure structure) {

        log.info("structure123 {}", structure.toString());
        List<FloorPlan> allByStructureAndType =
                floorPlanRepository.findAllByStructure(structure);

        log.info("allByStructureAndType {}", allByStructureAndType);
        return allByStructureAndType.stream()
                .map(FloorPlanResponse::of)
                .toList();
    }
}
