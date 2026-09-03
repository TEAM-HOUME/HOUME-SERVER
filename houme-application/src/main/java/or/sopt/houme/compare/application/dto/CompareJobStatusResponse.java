package or.sopt.houme.compare.application.dto;

import java.util.List;

public record CompareJobStatusResponse(
        String jobId,
        String status,
        SourcesStatusResponse sources,
        OriginalProductResponse originalProduct,
        JobResultResponse result
) {
    public record OriginalProductResponse(
            String title,
            String imageUrl,
            Double price,
            String currency
    ) {}

    public record JobResultResponse(
            int count,
            List<SimilarProductItemResponse> items
    ) {}

    public record SimilarProductItemResponse(
            String source,
            String title,
            String thumbnailUrl,
            Double price,
            String currency,
            String productUrl,
            Double score
    ) {}
}
