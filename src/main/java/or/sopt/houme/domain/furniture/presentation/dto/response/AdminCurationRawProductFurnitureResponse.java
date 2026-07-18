package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;
import or.sopt.houme.furniture.infra.persistence.FurnitureJpaEntity;

public record AdminCurationRawProductFurnitureResponse(
        Long mappingId,
        Long furnitureId,
        String furnitureNameKr,
        String furnitureNameEng,
        Long furnitureTypeId,
        String furnitureTypeNameKr
) {
    public static AdminCurationRawProductFurnitureResponse of(CurationRawProductFurniture mapping) {
        FurnitureJpaEntity furniture = mapping.getFurniture();

        return new AdminCurationRawProductFurnitureResponse(
                mapping.getId(),
                furniture != null ? furniture.getId() : null,
                furniture != null ? furniture.getFurnitureNameKr() : null,
                furniture != null ? furniture.getFurnitureNameEng() : null,
                furniture != null && furniture.getFurnitureType() != null ? furniture.getFurnitureType().getId() : null,
                furniture != null && furniture.getFurnitureType() != null ? furniture.getFurnitureType().getNameKr() : null
        );
    }
}
