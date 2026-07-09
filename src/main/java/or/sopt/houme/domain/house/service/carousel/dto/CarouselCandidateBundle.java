package or.sopt.houme.domain.house.service.carousel.dto;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;

import java.util.List;
import java.util.Map;

public record CarouselCandidateBundle(
        Long houseId,
        List<Long> selectedFurnitureIds,
        List<Long> furnitureCategoryIds,
        Map<SoozipCategory, List<Long>> otherCategoryIds,
        List<Long> fallbackIds
) {
}
