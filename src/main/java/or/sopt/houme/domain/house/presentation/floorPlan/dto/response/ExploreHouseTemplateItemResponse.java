package or.sopt.houme.domain.house.presentation.floorPlan.dto.response;

import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;

public record ExploreHouseTemplateItemResponse(
        Long id,
        String name,
        String imageUrl,
        Boolean isLatest
) {

    public static ExploreHouseTemplateItemResponse of(FloorPlan floorPlan, String imageUrl, boolean isLatest) {
        return new ExploreHouseTemplateItemResponse(
                floorPlan.getId(),
                floorPlan.getFloorPlanName(),
                imageUrl,
                isLatest
        );
    }
}
