package or.sopt.houme.compare.application.dto;

import java.util.List;

public record PresetDetailResponse(
        PresetOriginalProductResponse originalProduct,
        List<PresetSimilarProductResponse> similarProducts,
        long totalCount
) {}
