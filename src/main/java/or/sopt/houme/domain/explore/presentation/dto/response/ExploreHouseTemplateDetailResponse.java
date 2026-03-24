package or.sopt.houme.domain.explore.presentation.dto.response;

import java.util.List;

public record ExploreHouseTemplateDetailResponse(
        Long floorPlanId,
        String floorPlanName,
        String equilibrium,
        List<ExploreHouseTemplateDetailItemResponse> floorPlans
) {

    public static ExploreHouseTemplateDetailResponse of(
            Long floorPlanId,
            String floorPlanName,
            String equilibrium,
            List<ExploreHouseTemplateDetailItemResponse> floorPlans
    ) {
        return new ExploreHouseTemplateDetailResponse(
                floorPlanId,
                floorPlanName,
                equilibrium,
                floorPlans
        );
    }
}
