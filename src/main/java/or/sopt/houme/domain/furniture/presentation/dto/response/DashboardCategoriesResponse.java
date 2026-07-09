package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record DashboardCategoriesResponse(
        List<FurnitureCategoryGroup> categories
) {
    public static DashboardCategoriesResponse of(List<FurnitureCategoryGroup> categories) {
        return new DashboardCategoriesResponse(categories);
    }
}
