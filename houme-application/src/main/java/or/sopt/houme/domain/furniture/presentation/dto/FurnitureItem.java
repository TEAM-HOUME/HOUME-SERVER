package or.sopt.houme.domain.furniture.presentation.dto;

import or.sopt.houme.furniture.domain.FurnitureWithTypeView;

import java.util.Set;

public record FurnitureItem(
        Long id,
        String code,
        String label,
        Integer priority
) {
    // 이름에서 제거할 카테고리 목록 ("~~ 침대", "~~ 소파")
    private static final Set<String> REMOVABLE_CATEGORIES = Set.of("침대", "소파");

    public static FurnitureItem from(FurnitureWithTypeView furniture) {
        return from(furniture, furniture.priority());
    }

    public static FurnitureItem from(FurnitureWithTypeView furniture, Integer priority) {
        String categoryName = furniture.furnitureTypeNameKr();
        String rawLabel = furniture.furnitureNameKr();
        String cleanLabel = rawLabel;
        if (categoryName != null && REMOVABLE_CATEGORIES.contains(categoryName)) {
            cleanLabel = rawLabel.replace(" " + categoryName, "").trim();
        }
        return new FurnitureItem(
                furniture.id(),
                furniture.furnitureNameEng(),
                cleanLabel,
                priority
        );
    }
}
