package or.sopt.houme.domain.coupang.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.coupang.service.CoupangCollectionBatchService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/coupang")
@Tag(name = "어드민 쿠팡 수집 API")
public class CoupangCollectionAdminController {

    private final CoupangCollectionBatchService coupangCollectionBatchService;

    @PostMapping("/collection-jobs/run")
    @Operation(summary = "[TEMP] 쿠팡 상품 수집 배치 단건 실행")
    public ResponseEntity<ApiResponse<String>> runCollectionJob() {
        coupangCollectionBatchService.runOneJob();
        return ResponseEntity.ok(ApiResponse.ok("쿠팡 상품 수집 배치 1건 실행이 완료되었습니다."));
    }
}
