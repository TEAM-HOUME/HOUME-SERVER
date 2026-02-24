package or.sopt.houme.domain.house.service.floorPlan;

import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;

public interface FloorPlanService {

    // 집 구조 도면 서비스 제공 (Form, Structure)
    FloorPlanListResponse getHousingPlan(Form form, Structure structure);
}
