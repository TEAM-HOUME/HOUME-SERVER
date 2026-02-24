package or.sopt.houme.domain.furniture.presentation.dto;

import or.sopt.houme.domain.house.model.entity.enums.Activity;

public record ActivityItem(
        String code,
        String label

) {

    public static ActivityItem from(Activity activity) {
        return new ActivityItem(activity.name(), activity.getDescription());
    }
}
