package or.sopt.houme.domain.house.model.floorPlan.vo;

import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record FloorPlanImages(
        List<FloorPlanImageItem> items
) {

    public FloorPlanImages {
        if (items == null || items.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        List<FloorPlanImageItem> normalized = items.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(FloorPlanImageItem::sortOrder))
                .toList();
        if (normalized.size() != items.size()) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
        validateSortOrders(normalized);
        items = List.copyOf(normalized);
    }

    public static FloorPlanImages from(List<FloorPlanImageItem> items) {
        return new FloorPlanImages(items);
    }

    public static FloorPlanImages restore(List<FloorPlanImageItem> items, FloorPlanImageItem legacyImage) {
        if (items != null && !items.isEmpty()) {
            return new FloorPlanImages(items);
        }
        if (legacyImage != null) {
            return new FloorPlanImages(List.of(legacyImage));
        }
        throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
    }

    public FloorPlanImageItem representative() {
        return items.getFirst();
    }

    private static void validateSortOrders(List<FloorPlanImageItem> items) {
        Set<Integer> sortOrders = new LinkedHashSet<>();
        boolean duplicated = items.stream().anyMatch(item -> !sortOrders.add(item.sortOrder()));
        if (duplicated) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
    }
}
