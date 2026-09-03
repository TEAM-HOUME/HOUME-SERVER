package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import or.sopt.houme.compare.application.dto.CompareJobHistoryResponse;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/price-compare")
@Tag(name = "가격비교 API")
public class CompareJobHistoryController {

    @Operation(summary = "가격비교 검색 이력 조회")
    @GetMapping("/jobs/history")
    public ResponseEntity<ApiResponse<CompareJobHistoryResponse>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "3") int limit
    ) {
        // TODO: implement
        return ResponseEntity.ok(ApiResponse.ok(new CompareJobHistoryResponse(List.of())));
    }
}
