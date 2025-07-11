package or.sopt.houme.domain.furniture.dto;

import or.sopt.houme.domain.house.entity.enums.Activity;

public record ActivityItem(
        String type,
        String name

) {

    public static ActivityItem from(Activity activity) {
        return new ActivityItem(activity.name(), activity.getDescription());
    }
}
