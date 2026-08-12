package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record FurnitureCategoriesResponse(
        List<FurnitureCategoryResponse> categories
) {
    public static FurnitureCategoriesResponse of(List<FurnitureCategoryResponse> categories) {
        return new FurnitureCategoriesResponse(categories);
    }

    public record FurnitureCategoryResponse(
            Long id,
            String categoryName
    ) {
        public static FurnitureCategoryResponse of(Long id, String categoryName) {
            return new FurnitureCategoryResponse(id, categoryName);
        }
    }
}
