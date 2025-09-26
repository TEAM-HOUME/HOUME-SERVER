package or.sopt.houme.domain.furniture.dto;

import or.sopt.houme.domain.furniture.entity.Furniture;

public record FurnitureItem(
        Long id,    // furnitureId
        String code,    // 가구 한글명
        String label       // 가구 영어명
) {
    public static FurnitureItem from(Furniture furniture) {
        return new FurnitureItem(furniture.getId(), furniture.getFurnitureNameEng(), furniture.getFurnitureNameKr());
    }
}
