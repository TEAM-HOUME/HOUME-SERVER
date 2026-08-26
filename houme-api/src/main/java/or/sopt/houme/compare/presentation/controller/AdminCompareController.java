package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.AdminEbaySearchService;
import or.sopt.houme.compare.application.AdminEbaySearchService.AdminSearchCandidate;
import or.sopt.houme.compare.presentation.dto.request.AdminImageSearchRequest;
import or.sopt.houme.compare.presentation.dto.request.AdminTextSearchRequest;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Admin 가격비교 API")
@RequestMapping("/api/admin/v1/compare")
public class AdminCompareController {

    private final AdminEbaySearchService adminEbaySearchService;

    @Operation(
            summary = "eBay 텍스트 검색 결과 조회",
            description = "한글 상품명 → 키워드 번역 → eBay 검색 → 필터 → 상위 10개 유사도 스코어 반환. 파이프라인 로직 의사결정용."
    )
    @PostMapping("/text-search")
    public ResponseEntity<ApiResponse<List<AdminSearchCandidate>>> textSearch(
            @Valid @RequestBody AdminTextSearchRequest request
    ) {
        List<AdminSearchCandidate> results = adminEbaySearchService.textSearch(
                request.title(), request.imageUrl(), request.priceKrw(), request.category()
        );
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @Operation(
            summary = "eBay 이미지 검색 결과 조회",
            description = "이미지 URL → base64 변환 → eBay search_by_image → 필터 → 상위 10개 유사도 스코어 반환. 파이프라인 로직 의사결정용."
    )
    @PostMapping("/image-search")
    public ResponseEntity<ApiResponse<List<AdminSearchCandidate>>> imageSearch(
            @Valid @RequestBody AdminImageSearchRequest request
    ) {
        List<AdminSearchCandidate> results = adminEbaySearchService.imageSearch(
                request.imageUrl(), request.priceKrw(), request.category()
        );
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}
