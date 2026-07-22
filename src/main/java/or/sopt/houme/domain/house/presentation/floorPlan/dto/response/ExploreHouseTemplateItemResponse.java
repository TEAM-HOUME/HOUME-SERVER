package or.sopt.houme.domain.house.presentation.floorPlan.dto.response;


public record ExploreHouseTemplateItemResponse(
        Long id,
        String name,
        String imageUrl,
        Boolean isLatest
) {
}
