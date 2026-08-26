package or.sopt.houme.compare.presentation.dto.response;

import java.util.List;

public record JobResultResponse(
        int totalCount,
        List<SimilarProductItemResponse> similarProducts
) {}
