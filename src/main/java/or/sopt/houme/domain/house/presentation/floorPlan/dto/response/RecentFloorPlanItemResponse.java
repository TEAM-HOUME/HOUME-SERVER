package or.sopt.houme.domain.house.presentation.floorPlan.dto.response;

public record RecentFloorPlanItemResponse(
        String imageUrl,
        String view,
        Boolean isRecentUsedView
) {

    public static RecentFloorPlanItemResponse of(
            String imageUrl,
            String view,
            boolean isRecentUsedView
    ) {
        return new RecentFloorPlanItemResponse(imageUrl, view, isRecentUsedView);
    }
}
