package or.sopt.houme.domain.floorPlan.service;

import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;

public interface FloorPlanService {

    // 집 구조 도면 서비스 제공 (Form, Structure)
    FloorPlanListResponse getHousingPlan(Form form, Structure structure);
}
