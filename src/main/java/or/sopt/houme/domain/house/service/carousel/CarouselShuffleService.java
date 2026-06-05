package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.house.service.carousel.dto.CarouselCandidateBundle;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class CarouselShuffleService {

    private static final int TARGET_SIZE = 100;

    public List<Long> selectDisplayIds(CarouselCandidateBundle bundle, Long userId) {
        LinkedHashSet<Long> selectedIds = new LinkedHashSet<>();
        OrderedBucket selectedBucket = new OrderedBucket(bundle.selectedFurnitureIds());
        OrderedBucket furnitureBucket = new OrderedBucket(bundle.furnitureCategoryIds());

        Map<SoozipCategory, OrderedBucket> otherBuckets = new LinkedHashMap<>();
        for (Map.Entry<SoozipCategory, List<Long>> entry : bundle.otherCategoryIds().entrySet()) {
            otherBuckets.put(entry.getKey(), new OrderedBucket(entry.getValue()));
        }
        List<OrderedBucket> rotatingOtherBuckets = new ArrayList<>(otherBuckets.values());

        while (selectedIds.size() < TARGET_SIZE && hasAnyRemaining(selectedBucket, furnitureBucket)) {
            addNext(selectedIds, selectedBucket);
            addNext(selectedIds, selectedBucket);
            addNext(selectedIds, selectedBucket);
            addNext(selectedIds, selectedBucket);
            addNext(selectedIds, furnitureBucket);
        }

        int otherIndex = 0;
        while (selectedIds.size() < TARGET_SIZE && hasAnyRemaining(rotatingOtherBuckets)) {
            addNext(selectedIds, rotatingOtherBuckets.get(otherIndex % rotatingOtherBuckets.size()));
            otherIndex++;
        }

        OrderedBucket fallbackBucket = new OrderedBucket(bundle.fallbackIds());
        while (selectedIds.size() < TARGET_SIZE && fallbackBucket.hasNext()) {
            addNext(selectedIds, fallbackBucket);
        }

        return new ArrayList<>(selectedIds);
    }

    private boolean hasAnyRemaining(OrderedBucket selectedBucket, OrderedBucket furnitureBucket) {
        return selectedBucket.hasNext() || furnitureBucket.hasNext();
    }

    private boolean hasAnyRemaining(List<OrderedBucket> buckets) {
        return buckets.stream().anyMatch(OrderedBucket::hasNext);
    }

    private void addNext(Set<Long> selectedIds, OrderedBucket bucket) {
        while (bucket.hasNext()) {
            Long nextId = bucket.next();
            if (nextId != null && selectedIds.add(nextId)) {
                return;
            }
        }
    }

    private static final class OrderedBucket {
        private final List<Long> source;
        private final int size;
        private int attempts;

        private OrderedBucket(List<Long> candidateIds) {
            this.source = candidateIds == null ? List.of() : candidateIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            this.size = this.source.size();
            this.attempts = 0;
        }

        private boolean hasNext() {
            return attempts < size;
        }

        private Long next() {
            if (!hasNext()) {
                return null;
            }

            return source.get(attempts++);
        }
    }
}
