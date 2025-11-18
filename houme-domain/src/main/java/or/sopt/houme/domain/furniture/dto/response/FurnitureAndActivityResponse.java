package or.sopt.houme.domain.furniture.dto.response;

import or.sopt.houme.domain.furniture.dto.ActivityItem;
import or.sopt.houme.domain.furniture.dto.FurnitureGroup;

import java.util.List;

public record FurnitureAndActivityResponse(
        List<ActivityItem> activities,
        List<FurnitureCategoryGroup>  categories
) {
    // of
    public static FurnitureAndActivityResponse of(List<ActivityItem> activities, List<FurnitureCategoryGroup> furnitureCategories) {
        return new FurnitureAndActivityResponse(activities, furnitureCategories);
    }
}
