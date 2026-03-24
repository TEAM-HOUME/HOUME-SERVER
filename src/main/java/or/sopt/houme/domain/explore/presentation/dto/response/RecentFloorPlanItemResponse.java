package or.sopt.houme.domain.explore.presentation.dto.response;

import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;

public record RecentFloorPlanItemResponse(
        Long id,
        String name,
        String imageUrl,
        String equilibrium,
        String view
) {

    public static RecentFloorPlanItemResponse of(FloorPlan floorPlan, String imageUrl, String view) {
        return new RecentFloorPlanItemResponse(
                floorPlan.getId(),
                floorPlan.getFloorPlanName(),
                imageUrl,
                floorPlan.getEquilibrium() != null ? floorPlan.getEquilibrium().getDescription() : null,
                view
        );
    }
}
