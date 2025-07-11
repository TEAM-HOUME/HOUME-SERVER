package or.sopt.houme.domain.furniture.dto;

import or.sopt.houme.domain.furniture.entity.Furniture;

public record FurnitureItem(
        Long id,
        String type,
        String name
) {
    public static FurnitureItem from(Furniture furniture) {
        return new FurnitureItem(furniture.getId(), furniture.getFurnitureNameEng(), furniture.getFurnitureNameKr());
    }
}
