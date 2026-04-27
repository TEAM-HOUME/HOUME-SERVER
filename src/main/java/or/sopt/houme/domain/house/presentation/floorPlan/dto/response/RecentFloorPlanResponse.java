package or.sopt.houme.domain.house.presentation.floorPlan.dto.response;

public record RecentFloorPlanResponse(
        Boolean hasRecentImage,
        RecentFloorPlanItemResponse floorPlan
) {

    public static RecentFloorPlanResponse withRecent(RecentFloorPlanItemResponse floorPlan) {
        return new RecentFloorPlanResponse(Boolean.TRUE, floorPlan);
    }

    public static RecentFloorPlanResponse noRecent() {
        return new RecentFloorPlanResponse(Boolean.FALSE, RecentFloorPlanItemResponse.empty());
    }
}
