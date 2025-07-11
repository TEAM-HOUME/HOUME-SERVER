package or.sopt.houme.domain.furniture.dto.response;

import or.sopt.houme.domain.furniture.dto.ActivityItem;
import or.sopt.houme.domain.furniture.dto.FurnitureGroup;

import java.util.List;

public record FurnitureAndActivityResponse(
        List<ActivityItem> activities,
        FurnitureGroup beds,
        FurnitureGroup selectives
) {
    // of
    public static FurnitureAndActivityResponse of(List<ActivityItem> activities, FurnitureGroup beds, FurnitureGroup selectives) {
        return new FurnitureAndActivityResponse(activities, beds, selectives);
    }
}
