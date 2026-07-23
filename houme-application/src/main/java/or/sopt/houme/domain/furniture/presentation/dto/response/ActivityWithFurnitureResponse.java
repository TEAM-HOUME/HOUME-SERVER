package or.sopt.houme.domain.furniture.presentation.dto.response;

import or.sopt.houme.domain.furniture.presentation.dto.FurnitureItem;
import or.sopt.houme.domain.house.model.entity.enums.Activity;

import java.util.List;

public record ActivityWithFurnitureResponse(
        String code,
        String label,
        List<FurnitureItem> furnitures
) {
    public static ActivityWithFurnitureResponse of(Activity activity, List<FurnitureItem> furnitures) {
        return new ActivityWithFurnitureResponse(activity.name(), activity.getDescription(), furnitures);
    }
}
