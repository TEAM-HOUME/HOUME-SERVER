package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import or.sopt.houme.compare.application.dto.CreateCompareJobRequest;
import or.sopt.houme.compare.application.dto.CompareJobStatusResponse;
import or.sopt.houme.compare.application.dto.CreateJobResponse;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/price-compare")
@Tag(name = "가격비교 API")
public class PriceCompareController {

    @Operation(summary = "가격비교 Job 생성 (202 Accepted)")
    @PostMapping("/jobs")
    public ResponseEntity<ApiResponse<CreateJobResponse>> createJob(
            @RequestBody CreateCompareJobRequest request
    ) {
        // TODO: implement
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new ApiResponse<>(202, "응답 성공", null));
    }

    @Operation(summary = "가격비교 Job 상태 조회")
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<CompareJobStatusResponse>> getJob(@PathVariable String jobId) {
        // TODO: implement
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
