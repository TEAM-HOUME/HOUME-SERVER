package or.sopt.houme.domain.explore.presentation.dto.response;

public record RecentFloorPlanResponse(
        Boolean hasRecentImage,
        Object floorPlan
) {

    public static RecentFloorPlanResponse withRecent(RecentFloorPlanItemResponse floorPlan) {
        return new RecentFloorPlanResponse(Boolean.TRUE, floorPlan);
    }

    public static RecentFloorPlanResponse noRecent() {
        return new RecentFloorPlanResponse(Boolean.FALSE, Boolean.FALSE);
    }
}
