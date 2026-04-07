package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record CurationProductFilterResponse(
        List<FurnitureTypeFilterResponse> furnitureTypes,
        List<PriceRangeFilterResponse> priceRanges,
        List<ColorFilterResponse> colors
) {
}
