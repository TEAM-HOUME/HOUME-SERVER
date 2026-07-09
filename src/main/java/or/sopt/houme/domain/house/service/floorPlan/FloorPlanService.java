package or.sopt.houme.domain.house.service.floorPlan;

import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateDetailResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.RecentFloorPlanResponse;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.user.model.entity.User;

public interface FloorPlanService {

    // 집 구조 도면 서비스 제공 (Form, Structure)
    FloorPlanListResponse getHousingPlan(Form form, Structure structure);

    RecentFloorPlanResponse getRecentFloorPlan(User user);

    ExploreHouseTemplateListResponse getExploreHouseTemplates(
            Integer size,
            Form residenceType,
            Structure layoutType,
            Equilibrium equilibrium,
            User user
    );

    ExploreHouseTemplateDetailResponse getExploreHouseTemplateDetail(Long floorPlanId);
}
