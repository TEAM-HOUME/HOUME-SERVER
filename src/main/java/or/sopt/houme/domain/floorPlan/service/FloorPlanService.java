package or.sopt.houme.domain.floorPlan.service;

import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanResponse;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;

import java.util.List;

public interface FloorPlanService {

    // 집 구조 도면 서비스 제공 (Form, Structure)
    List<FloorPlanResponse> getHousingPlan(Form form, Structure structure);
}
