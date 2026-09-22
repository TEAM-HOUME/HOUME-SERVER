package or.sopt.houme.compare.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.JobStatus;
import or.sopt.houme.compare.domain.SimilarProduct;
import or.sopt.houme.global.api.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CompareJobResponse(
        String jobId,
        String status,
        String currentStage,
        SourcesStatusResponse sources,
        Integer errorCode,
        String errorMessage,
        OriginalProductResponse originalProduct,
        JobResultResponse result
) {
    public static CompareJobResponse from(CompareJob job) {
        ErrorCode ec = job.getStatus() == JobStatus.FAILED ? job.getErrorCode() : null;
        return new CompareJobResponse(
                job.getJobId(),
                job.getStatus().name(),
                job.getCurrentStage() != null ? job.getCurrentStage().name() : null,
                new SourcesStatusResponse(job.getEbayStatus(), job.getCoupangStatus(), job.getCatalogStatus()),
                ec != null ? ec.getCode() : null,
                ec != null ? ec.getMsg() : null,
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
