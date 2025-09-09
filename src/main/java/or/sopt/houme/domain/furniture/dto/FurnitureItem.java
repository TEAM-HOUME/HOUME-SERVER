package or.sopt.houme.domain.furniture.dto;

import or.sopt.houme.domain.furniture.entity.Furniture;

public record FurnitureItem(
        Long id,
        String code,
        String label
) {
    public static FurnitureItem from(Furniture furniture) {
        return new FurnitureItem(furniture.getId(), furniture.getFurnitureNameEng(), furniture.getFurnitureNameKr());
    }
}
