package or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response;

import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;

public record AdminFloorPlanImageResponse(
        String url,
        String filename,
        String originalFilename,
        String fileExtension,
        Integer sortOrder,
        String view
) {
    public static AdminFloorPlanImageResponse of(FloorPlanImageItem item) {
        return new AdminFloorPlanImageResponse(
                item.url(),
                item.filename(),
                item.originalFilename(),
                item.fileExtension(),
                item.sortOrder(),
                item.view()
        );
    }
}
