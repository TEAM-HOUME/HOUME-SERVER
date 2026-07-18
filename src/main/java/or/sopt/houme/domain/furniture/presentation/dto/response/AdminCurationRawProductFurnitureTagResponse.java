package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.furniture.infra.persistence.FurnitureJpaEntity;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;

public record AdminCurationRawProductFurnitureTagResponse(
        Long mappingId,
        Long furnitureTagId,
        Long furnitureId,
        String furnitureNameKr,
        Long furnitureTypeId,
        String furnitureTypeNameKr,
        Long tagId,
        String tagNameKr,
        Integer priority,
        String searchKeyword
) {
    /**
     * #582: FurnitureTag→Tag 연관 절단으로 태그명은 tagId 로 별도 조회해 주입한다.
     * @param tagNameKr furnitureTag.getTagId() 로 조회한 태그 한글명(없으면 null)
     */
    public static AdminCurationRawProductFurnitureTagResponse of(CurationRawProductFurnitureTag mapping, String tagNameKr) {
        FurnitureTag furnitureTag = mapping.getFurnitureTag();
        FurnitureJpaEntity furniture = furnitureTag != null ? furnitureTag.getFurniture() : null;

        return new AdminCurationRawProductFurnitureTagResponse(
                mapping.getId(),
                furnitureTag != null ? furnitureTag.getId() : null,
                furniture != null ? furniture.getId() : null,
                furniture != null ? furniture.getFurnitureNameKr() : null,
                furniture != null && furniture.getFurnitureType() != null ? furniture.getFurnitureType().getId() : null,
                furniture != null && furniture.getFurnitureType() != null ? furniture.getFurnitureType().getNameKr() : null,
                furnitureTag != null ? furnitureTag.getTagId() : null,
                tagNameKr,
                furnitureTag != null ? furnitureTag.getPriority() : null,
                furnitureTag != null ? furnitureTag.getSearchKeyword() : null
        );
    }
}
