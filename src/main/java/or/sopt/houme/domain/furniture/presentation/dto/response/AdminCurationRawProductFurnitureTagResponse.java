package or.sopt.houme.domain.furniture.presentation.dto.response;


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
}
