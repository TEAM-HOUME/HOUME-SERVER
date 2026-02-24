package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.service.dto.CurationRawProductSaveResult;

public record SoozipRawProductSaveResponse(
        String source,
        SoozipCategory category,
        int productCount,
        int insertedCount,
        int updatedCount,
        int skippedCount
) {
    public static SoozipRawProductSaveResponse of(
            String source,
            SoozipCategory category,
            int productCount,
            CurationRawProductSaveResult result
    ) {
        return new SoozipRawProductSaveResponse(
                source,
                category,
                productCount,
                result.insertedCount(),
                result.updatedCount(),
                result.skippedCount()
        );
    }
}
