package or.sopt.houme.domain.furniture.presentation.dto;

import or.sopt.houme.domain.furniture.model.entity.Furniture;

import java.util.Set;

public record FurnitureItem(
        Long id,
        String code,
        String label,
        Integer priority
) {
    // 이름에서 제거할 카테고리 목록 ("~~ 침대", "~~ 소파")
    private static final Set<String> REMOVABLE_CATEGORIES = Set.of("침대", "소파");

    public static FurnitureItem from(Furniture furniture) {
        return from(furniture, furniture.getPriority());
    }

    public static FurnitureItem from(Furniture furniture, Integer priority) {
        String categoryName = furniture.getFurnitureType().getNameKr();
        String rawLabel = furniture.getFurnitureNameKr();

        String cleanLabel = rawLabel;

        if (REMOVABLE_CATEGORIES.contains(categoryName)) {
            cleanLabel = rawLabel.replace(" " + categoryName, "").trim();
        }

        return new FurnitureItem(
                furniture.getId(),
                furniture.getFurnitureNameEng(),
                cleanLabel,
                priority
        );
    }
}
