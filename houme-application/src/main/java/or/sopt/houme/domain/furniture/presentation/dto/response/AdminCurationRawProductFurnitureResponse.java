package or.sopt.houme.domain.furniture.presentation.dto.response;


public record AdminCurationRawProductFurnitureResponse(
        Long mappingId,
        Long furnitureId,
        String furnitureNameKr,
        String furnitureNameEng,
        Long furnitureTypeId,
        String furnitureTypeNameKr
) {
}
