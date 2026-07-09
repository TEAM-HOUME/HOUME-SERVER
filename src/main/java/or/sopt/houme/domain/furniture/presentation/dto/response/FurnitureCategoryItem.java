package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.presentation.dto.FurnitureItem;

public record FurnitureCategoryItem(
        Long id,
        String code,
        String label
) {
    public static FurnitureCategoryItem from(FurnitureItem item) {
        return new FurnitureCategoryItem(
                item.id(),
                item.code(),
                item.label()
        );
    }
}
