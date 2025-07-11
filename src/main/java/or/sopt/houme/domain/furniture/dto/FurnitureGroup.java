package or.sopt.houme.domain.furniture.dto;

import java.util.List;

public record FurnitureGroup(
        boolean isRequired,
        List<FurnitureItem> items
) {

    public static FurnitureGroup from(boolean isRequired, List<FurnitureItem> items) {
        return new FurnitureGroup(isRequired, items);
    }
}
