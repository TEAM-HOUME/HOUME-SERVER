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
        return new CompareJobResponse(
                job.getJobId(),
                job.getStatus().name(),
                new SourcesStatusResponse(job.getEbayStatus(), job.getCoupangStatus(), job.getCatalogStatus()),
                OriginalProductResponse.from(job.getOriginalProduct()),
                buildResult(job)
        );
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
