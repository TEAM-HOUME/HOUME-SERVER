package or.sopt.houme.domain.furniture.presentation.dto.response;

import java.util.List;

public record CurationProductMetaResponse(
        Long nextCursor,
        Boolean hasNext,
        List<CurationProductAppliedFilterResponse> appliedFilters,
        Boolean isRecommended
) {
}
