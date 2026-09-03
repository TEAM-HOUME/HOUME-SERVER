package or.sopt.houme.compare.presentation.dto.response;

import java.util.List;

public record PresetDetailResponse(
        PresetOriginalProductResponse originalProduct,
        List<PresetSimilarProductResponse> similarProducts,
        long totalCount
) {}
