package or.sopt.houme.domain.explore.presentation.dto.response;

import java.util.List;

public record ExploreHouseTemplateListResponse(
        Boolean isExact,
        List<ExploreHouseTemplateItemResponse> floorPlans
) {

    public static ExploreHouseTemplateListResponse of(
            boolean isExact,
            List<ExploreHouseTemplateItemResponse> floorPlans
    ) {
        return new ExploreHouseTemplateListResponse(isExact, floorPlans);
    }
}
