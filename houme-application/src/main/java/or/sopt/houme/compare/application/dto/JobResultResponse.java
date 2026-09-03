package or.sopt.houme.compare.application.dto;

import java.util.List;

public record JobResultResponse(
        int totalCount,
        List<SimilarProductItemResponse> similarProducts
) {}
