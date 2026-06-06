package or.sopt.houme.domain.house.presentation.floorPlan.dto.response;

import java.util.List;

public record RecentFloorPlanResponse(
        Boolean hasRecentImage,
        Long floorPlanId,
        String floorPlanName,
        String equilibrium,
        List<RecentFloorPlanItemResponse> floorPlans
) {

    public static RecentFloorPlanResponse withRecent(
            Long floorPlanId,
            String floorPlanName,
            String equilibrium,
            List<RecentFloorPlanItemResponse> floorPlans
    ) {
        return new RecentFloorPlanResponse(Boolean.TRUE, floorPlanId, floorPlanName, equilibrium, floorPlans);
    }

    public static RecentFloorPlanResponse noRecent() {
        return new RecentFloorPlanResponse(Boolean.FALSE, null, null, null, List.of());
    }
}
