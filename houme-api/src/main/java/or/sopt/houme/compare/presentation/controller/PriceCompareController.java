package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.CompareJob;
import or.sopt.houme.compare.domain.OriginalProduct;
import or.sopt.houme.compare.domain.port.in.PriceCompareUseCase;
import or.sopt.houme.compare.domain.port.out.SaveCompareHistoryPort;
import jakarta.validation.Valid;
import or.sopt.houme.compare.application.dto.CreateCompareJobRequest;
import or.sopt.houme.compare.application.dto.CompareJobResponse;
import or.sopt.houme.compare.application.dto.CreateJobResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/price-compare")
@RequiredArgsConstructor
@Tag(name = "가격비교 API")
public class PriceCompareController {

    private final PriceCompareUseCase priceCompareUseCase;
    private final SaveCompareHistoryPort saveCompareHistoryPort;

    @Operation(summary = "가격비교 Job 생성 (202 Accepted)")
    @PostMapping("/jobs")
    public ResponseEntity<ApiResponse<CreateJobResponse>> createJob(
            @RequestBody @Valid CreateCompareJobRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CompareJob job = priceCompareUseCase.createJobByUrl(request.url());
        OriginalProduct op = job.getOriginalProduct();
        Long price = op != null && op.price() != null ? op.price().longValue() : null;

        saveCompareHistoryPort.save(
                userDetails.getUser().getId(),
                job.getSourceUrl(),
                op != null ? op.title() : null,
                op != null ? op.imageUrl() : null,
                price
        );

        CreateJobResponse response = new CreateJobResponse(
                job.getJobId(),
                job.getStatus().name(),
                job.getSourceUrl(),
                op != null ? op.title() : null,
                op != null ? op.imageUrl() : null,
                price
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
