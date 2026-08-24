package or.sopt.houme.coupang.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.coupang.presentation.dto.CoupangProductSearchResponse;
import or.sopt.houme.coupang.service.CoupangProductSearchService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 배치 실행 없이 쿠팡 파트너스 인증·응답을 확인하기 위한 임시 관리자 API입니다. */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/coupang/products")
@Tag(name = "임시 쿠팡 상품 검색 API")
public class AdminCoupangProductController {

    private final CoupangProductSearchService coupangProductSearchService;

    @GetMapping
    @Operation(summary = "쿠팡 파트너스 상품 단건 검색", description = "배치 큐를 거치거나 DB를 변경하지 않고 쿠팡 파트너스 검색 API를 즉시 호출합니다.")
    public ResponseEntity<ApiResponse<List<CoupangProductSearchResponse>>> searchProducts(
            @RequestParam @NotBlank @Size(max = 100) String keyword,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        List<CoupangProductSearchResponse> response = coupangProductSearchService.search(keyword, limit)
                .stream()
                .map(CoupangProductSearchResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
