package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.AdminCurationEmbeddingUseCase;
import or.sopt.houme.compare.application.AdminEbaySearchService;
import or.sopt.houme.compare.application.AdminEbaySearchService.AdminSearchResult;
import or.sopt.houme.compare.application.AdminKeywordCheckUseCase;
import or.sopt.houme.compare.application.dto.AdminImageSearchRequest;
import or.sopt.houme.compare.application.dto.AdminTextSearchRequest;
import or.sopt.houme.compare.application.dto.KeywordCheckRequest;
import or.sopt.houme.compare.application.dto.KeywordCheckResponse;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Tag(name = "Admin 가격비교 API")
@RequestMapping("/api/admin/v1/compare")
public class AdminCompareController {

    private final AdminEbaySearchService adminEbaySearchService;
    private final AdminKeywordCheckUseCase adminKeywordCheckUseCase;
    private final AdminCurationEmbeddingUseCase adminCurationEmbeddingUseCase;

    @Operation(
            summary = "eBay 텍스트 검색 결과 조회",
            description = "한글 상품명 → 키워드 번역 → eBay 검색 → 필터 → 상위 10개 유사도 스코어 반환. 파이프라인 로직 의사결정용."
    )
    @PostMapping("/text-search")
    public ResponseEntity<ApiResponse<AdminSearchResult>> textSearch(
            @Valid @RequestBody AdminTextSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminEbaySearchService.textSearch(request.title(), request.imageUrl(), request.priceKrw(), request.category())
        ));
    }

    @Operation(
            summary = "eBay 이미지 검색 결과 조회",
            description = "이미지 URL → base64 변환 → eBay search_by_image → 필터 → 상위 10개 유사도 스코어 반환. 파이프라인 로직 의사결정용."
    )
    @PostMapping("/image-search")
    public ResponseEntity<ApiResponse<AdminSearchResult>> imageSearch(
            @Valid @RequestBody AdminImageSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminEbaySearchService.imageSearch(request.imageUrl(), request.priceKrw(), request.category())
        ));
    }

    @Operation(summary = "자체 카탈로그 임베딩 배치 즉시 실행", description = "비동기 실행. limit개 처리 후 종료. 실행 중 중복 요청은 409 반환.")
    @PostMapping("/curation-embedding/trigger")
    public ResponseEntity<ApiResponse<String>> triggerEmbedding(
            @RequestParam(defaultValue = "100") int limit
    ) {
        if (limit < 1) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(400, "limit은 1 이상이어야 합니다."));
        }
        if (adminCurationEmbeddingUseCase.isRunning()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail(409, "이미 실행 중입니다. 완료 후 다시 시도해주세요."));
        }
        adminCurationEmbeddingUseCase.triggerEmbeddingAsync(limit);
        return ResponseEntity.accepted().body(ApiResponse.ok(limit + "개 임베딩 처리 시작 (백그라운드 실행 중)"));
    }

    @Operation(
            summary = "Gemini 검색어 검증",
            description = "한글 상품명 → Gemini 번역 → eBay 영문 키워드 + 쿠팡 한글 키워드 반환. 검색어 품질 확인용."
    )
    @PostMapping("/keyword-check")
    public ResponseEntity<ApiResponse<KeywordCheckResponse>> keywordCheck(
            @Valid @RequestBody KeywordCheckRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminKeywordCheckUseCase.check(request.productName())));
    }
}
