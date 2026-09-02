package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.OriginalProduct;
import or.sopt.houme.compare.domain.port.in.PriceCompareUseCase;
import or.sopt.houme.compare.presentation.dto.request.CreateCompareJobRequest;
import or.sopt.houme.compare.presentation.dto.request.DummyProductInput;
import or.sopt.houme.compare.presentation.dto.response.CompareJobResponse;
import or.sopt.houme.compare.presentation.dto.response.CreateJobResponse;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CompareException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/price-compare")
@RequiredArgsConstructor
@Tag(name = "가격비교 API")
public class PriceCompareController {

    private final PriceCompareUseCase priceCompareUseCase;
    private final Environment environment;

    @Operation(summary = "가격비교 Job 생성 (202 Accepted)")
    @PostMapping("/jobs")
    public ResponseEntity<ApiResponse<CreateJobResponse>> createJob(
            @RequestBody CreateCompareJobRequest request
    ) {
        CompareJob job;

        if (request.dummyProduct() != null) {
            // prod 프로파일에서는 더미 모드 금지
            if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
                throw new CompareException(ErrorCode.COMPARE_DUMMY_NOT_ALLOWED_IN_PROD);
            }
            DummyProductInput d = request.dummyProduct();
            OriginalProduct dummy = OriginalProduct.of(d.title(), d.imageUrl(), d.price(), d.category());
            job = priceCompareUseCase.createJobByDummy(dummy);
        } else {
            job = priceCompareUseCase.createJobByUrl(request.url());
        }

        OriginalProduct op = job.getOriginalProduct();
        CreateJobResponse response = new CreateJobResponse(
                job.getJobId(),
                job.getStatus().name(),
                job.getSourceUrl(),
                op != null ? op.title() : null,
                op != null ? op.imageUrl() : null,
                op != null && op.price() != null ? op.price().longValue() : null,
                null
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ApiResponse<>(202, "응답 성공", response));
    }

    @Operation(summary = "가격비교 Job 상태 조회")
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<CompareJobResponse>> getJob(@PathVariable String jobId) {
        CompareJob job = priceCompareUseCase.getJob(jobId);
        return ResponseEntity.ok(ApiResponse.ok(CompareJobResponse.from(job)));
    }

}
