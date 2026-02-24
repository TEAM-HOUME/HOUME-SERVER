package or.sopt.houme.domain.house.service.floorPlan;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanResponse;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FloorPlanServiceImpl implements FloorPlanService {

    private final FloorPlanRepository floorPlanRepository;

    // 집 구조 도면 제공 서비스 (조건에 받아서)
    @Cacheable(
            value = "floorPlanListCache",
            key = "'structure:' + #structure.name()"
    )
    @Override
    public FloorPlanListResponse getHousingPlan(Form form, Structure structure) {

        List<FloorPlan> allByStructureAndType =
                floorPlanRepository.findAllByStructure(structure);

        List<FloorPlanResponse> list = allByStructureAndType.stream()
                .map(FloorPlanResponse::of)
                .toList();

        return new FloorPlanListResponse(list);
    }
}
