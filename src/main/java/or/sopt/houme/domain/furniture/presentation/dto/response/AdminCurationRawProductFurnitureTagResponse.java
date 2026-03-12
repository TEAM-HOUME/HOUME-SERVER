package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.house.model.taste.entity.Tag;

public record AdminCurationRawProductFurnitureTagResponse(
        Long mappingId,
        Long furnitureTagId,
        Long furnitureId,
        String furnitureNameKr,
        Long tagId,
        String tagNameKr,
        Integer priority,
        String searchKeyword
) {
    public static AdminCurationRawProductFurnitureTagResponse of(CurationRawProductFurnitureTag mapping) {
        FurnitureTag furnitureTag = mapping.getFurnitureTag();
        Furniture furniture = furnitureTag != null ? furnitureTag.getFurniture() : null;
        Tag tag = furnitureTag != null ? furnitureTag.getTag() : null;

        return new AdminCurationRawProductFurnitureTagResponse(
                mapping.getId(),
                furnitureTag != null ? furnitureTag.getId() : null,
                furniture != null ? furniture.getId() : null,
                furniture != null ? furniture.getFurnitureNameKr() : null,
                tag != null ? tag.getId() : null,
                tag != null ? tag.getTagNameKr() : null,
                furnitureTag != null ? furnitureTag.getPriority() : null,
                furnitureTag != null ? furnitureTag.getSearchKeyword() : null
        );
    }
}
