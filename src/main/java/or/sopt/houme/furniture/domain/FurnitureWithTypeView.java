package or.sopt.houme.furniture.domain;

/** 가구+타입 평탄화 read model (대시보드/분류 라벨용). */
public record FurnitureWithTypeView(
        Long id,
        String furnitureNameEng,
        String furnitureNameKr,
        Integer priority,
        Long furnitureTypeId,
        String furnitureTypeNameKr,
        String furnitureTypeNameEng
) {
}
