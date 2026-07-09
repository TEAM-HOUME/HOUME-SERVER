package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record ActivityFurnitureMappingsResponse(
        List<ActivityWithFurnitureResponse> activities
) {
    public static ActivityFurnitureMappingsResponse of(List<ActivityWithFurnitureResponse> activities) {
        return new ActivityFurnitureMappingsResponse(activities);
    }
}
