package or.sopt.houme.domain.house.service.carousel;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.house.service.carousel.dto.CarouselCandidateBundle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarouselCandidateService {

    private static final int SELECTED_FURNITURE_CANDIDATE_LIMIT = 160;
    private static final int FURNITURE_CATEGORY_CANDIDATE_LIMIT = 160;
    private static final int OTHER_CATEGORY_CANDIDATE_LIMIT = 60;
    private static final int FALLBACK_CANDIDATE_LIMIT = 220;

    private static final List<SoozipCategory> OTHER_CATEGORIES = List.of(
            SoozipCategory.LIGHTING,
            SoozipCategory.LIVING_GOODS,
            SoozipCategory.HOME_FABRIC,
            SoozipCategory.ACCESSORY,
            SoozipCategory.MINI_ELECTRONICS
    );

    private final CurationRawProductRepository curationRawProductRepository;

    public CarouselCandidateBundle collectCandidates(Long userId, List<Long> requestFurnitureIds) {
        List<Long> selectedFurnitureSourceIds = normalizeFurnitureIds(requestFurnitureIds);
        Set<Long> excludedIds = new LinkedHashSet<>();

        List<Long> selectedFurnitureIds = mapIds(
                curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByFurnitureIds(
                        userId,
                        selectedFurnitureSourceIds,
                        SoozipCategory.FURNITURE,
                        SELECTED_FURNITURE_CANDIDATE_LIMIT,
                        List.copyOf(excludedIds)
                )
        );
        excludedIds.addAll(selectedFurnitureIds);

        List<Long> furnitureCategoryIds = mapIds(
                curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                        userId,
                        SoozipCategory.FURNITURE,
                        FURNITURE_CATEGORY_CANDIDATE_LIMIT,
                        List.copyOf(excludedIds)
                )
        );
        excludedIds.addAll(furnitureCategoryIds);

        Map<SoozipCategory, List<Long>> otherCategoryIds = new EnumMap<>(SoozipCategory.class);
        for (SoozipCategory category : OTHER_CATEGORIES) {
            List<Long> otherIds = mapIds(curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                    userId,
                    category,
                    OTHER_CATEGORY_CANDIDATE_LIMIT,
                    List.copyOf(excludedIds)
            ));
            otherCategoryIds.put(category, otherIds);
            excludedIds.addAll(otherIds);
        }

        List<Long> fallbackIds = mapIds(
                curationRawProductRepository.findExposedRawProductsExcludingLikedByUserByCategory(
                        userId,
                        null,
                        FALLBACK_CANDIDATE_LIMIT,
                        List.copyOf(excludedIds)
                )
        );

        return new CarouselCandidateBundle(
                null,
                selectedFurnitureIds,
                furnitureCategoryIds,
                otherCategoryIds,
                fallbackIds
        );
    }

    private List<Long> normalizeFurnitureIds(List<Long> furnitureIds) {
        if (furnitureIds == null || furnitureIds.isEmpty()) {
            return List.of();
        }

        return furnitureIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<Long> mapIds(List<CurationRawProduct> rawProducts) {
        return rawProducts.stream()
                .map(CurationRawProduct::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
