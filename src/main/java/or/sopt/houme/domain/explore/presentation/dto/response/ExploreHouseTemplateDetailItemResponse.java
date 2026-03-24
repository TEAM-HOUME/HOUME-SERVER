package or.sopt.houme.domain.explore.presentation.dto.response;

public record ExploreHouseTemplateDetailItemResponse(
        String imageUrl,
        String view
) {

    public static ExploreHouseTemplateDetailItemResponse of(
            String imageUrl,
            String view
    ) {
        return new ExploreHouseTemplateDetailItemResponse(imageUrl, view);
    }
}
