package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.house.service.carousel.dto.CarouselCandidateBundle;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public List<Long> selectDisplayIds(CarouselCandidateBundle bundle, Long userId) {
        LinkedHashSet<Long> selectedIds = new LinkedHashSet<>();
        long epochSecond = ZonedDateTime.now(KST).toEpochSecond();
        ShuffledBucket selectedBucket = new ShuffledBucket(bundle.selectedFurnitureIds(), computeSeed(userId, bundle.houseId(), epochSecond, 11L));
        ShuffledBucket furnitureBucket = new ShuffledBucket(bundle.furnitureCategoryIds(), computeSeed(userId, bundle.houseId(), epochSecond, 23L));

        Map<SoozipCategory, ShuffledBucket> otherBuckets = new LinkedHashMap<>();
        long salt = 31L;
        for (Map.Entry<SoozipCategory, List<Long>> entry : bundle.otherCategoryIds().entrySet()) {
            otherBuckets.put(entry.getKey(), new ShuffledBucket(entry.getValue(), computeSeed(userId, bundle.houseId(), epochSecond, salt++)));
        }
        List<ShuffledBucket> rotatingOtherBuckets = new ArrayList<>(otherBuckets.values());

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

        ShuffledBucket fallbackBucket = new ShuffledBucket(bundle.fallbackIds(), computeSeed(userId, bundle.houseId(), epochSecond, 97L));
        while (selectedIds.size() < TARGET_SIZE && fallbackBucket.hasNext()) {
            addNext(selectedIds, fallbackBucket);
        }

        return new ArrayList<>(selectedIds);
    }

    private boolean hasAnyRemaining(ShuffledBucket selectedBucket, ShuffledBucket furnitureBucket) {
        return selectedBucket.hasNext() || furnitureBucket.hasNext();
    }

    private boolean hasAnyRemaining(List<ShuffledBucket> buckets) {
        return buckets.stream().anyMatch(ShuffledBucket::hasNext);
    }

    private void addNext(Set<Long> selectedIds, ShuffledBucket bucket) {
        while (bucket.hasNext()) {
            Long nextId = bucket.next();
            if (nextId != null && selectedIds.add(nextId)) {
                return;
            }
        }
    }

    private long computeSeed(Long userId, Long houseId, long epochSecond, long salt) {
        long baseUserId = userId == null ? 0L : userId;
        long baseHouseId = houseId == null ? 0L : houseId;
        return (baseUserId * 31L) ^ (baseHouseId * 17L) ^ (epochSecond * 13L) ^ salt;
    }

    private static final class ShuffledBucket {
        private final List<Long> source;
        private final int size;
        private final int start;
        private final int step;
        private int attempts;

        private ShuffledBucket(List<Long> candidateIds, long seed) {
            this.source = candidateIds == null ? List.of() : candidateIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            this.size = this.source.size();
            this.start = size == 0 ? 0 : Math.floorMod(seed, size);
            this.step = size <= 1 ? 1 : resolveStep(seed, size);
            this.attempts = 0;
        }

        private boolean hasNext() {
            return attempts < size;
        }

        private Long next() {
            if (!hasNext()) {
                return null;
            }

            int index = size == 0 ? 0 : Math.floorMod(start + (attempts * step), size);
            attempts++;
            return source.get(index);
        }

        private int resolveStep(long seed, int size) {
            int candidate = (int) Math.floorMod((seed * 7L) + 5L, size);
            if (candidate == 0) {
                candidate = 1;
            }
            while (gcd(candidate, size) != 1) {
                candidate++;
                if (candidate >= size) {
                    candidate = 1;
                }
            }
            return candidate;
        }

        private int gcd(int left, int right) {
            int first = Math.abs(left);
            int second = Math.abs(right);
            while (second != 0) {
                int remainder = first % second;
                first = second;
                second = remainder;
            }
            return first == 0 ? 1 : first;
        }
    }
}
