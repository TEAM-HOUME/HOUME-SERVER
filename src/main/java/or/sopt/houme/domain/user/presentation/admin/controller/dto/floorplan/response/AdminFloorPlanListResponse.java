package or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response;

import java.util.List;

public record AdminFloorPlanListResponse(
        List<AdminFloorPlanResponse> floorPlans
) {
}
