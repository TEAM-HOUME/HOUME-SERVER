package or.sopt.houme.domain.house.model.floorPlan.vo;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

public record FloorPlanImageItem(
        String url,
        String filename,
        String originalFilename,
        String fileExtension,
        Integer sortOrder
) {

    public static FloorPlanImageItem create(
            String url,
            String filename,
            String originalFilename,
            String fileExtension,
            Integer sortOrder
    ) {
        if (sortOrder == null || sortOrder < 1) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return new FloorPlanImageItem(
                normalizeRequired(url),
                normalizeRequired(filename),
                normalizeRequired(originalFilename),
                normalizeRequired(fileExtension).toLowerCase(),
                sortOrder
        );
    }

    private static String normalizeRequired(String value) {
        if (value == null) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        return trimmed;
    }
}
