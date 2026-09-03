package or.sopt.houme.compare.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.JobStatus;
import or.sopt.houme.compare.domain.SimilarProduct;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompareJobResponse(
        String jobId,
        String status,
        SourcesStatusResponse sources,
        OriginalProductResponse originalProduct,
        JobResultResponse result
) {
    public static CompareJobResponse from(CompareJob job) {
        String ebayStatus = resolveEbayStatus(job);
        JobResultResponse result = buildResult(job);

        return new CompareJobResponse(
                job.getJobId(),
                job.getStatus().name(),
                SourcesStatusResponse.of(ebayStatus),
                OriginalProductResponse.from(job.getOriginalProduct()),
                result
        );
    }

    private static String resolveEbayStatus(CompareJob job) {
        return switch (job.getStatus()) {
            case PENDING  -> "WAITING";
            case RUNNING  -> "RUNNING";
            case DONE     -> "DONE";
            case FAILED   -> "FAILED";
        };
    }

    private static JobResultResponse buildResult(CompareJob job) {
        if (job.getStatus() != JobStatus.DONE) return null;
        List<SimilarProduct> products = job.getSimilarProducts();
        if (products == null) return null;
        List<SimilarProductItemResponse> items = products.stream()
                .map(SimilarProductItemResponse::from)
                .collect(Collectors.toList());
        return new JobResultResponse(items.size(), items);
    }
}
