package or.sopt.houme.domain.house.model.floorPlan.vo;

public record FloorPlanImageItem(
        String url,
        String filename,
        String originalFilename,
        String fileExtension,
        Integer sortOrder
) {
}
