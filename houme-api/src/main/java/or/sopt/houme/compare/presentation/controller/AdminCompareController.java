package or.sopt.houme.compare.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.AdminEbaySearchService;
import or.sopt.houme.compare.application.AdminEbaySearchService.AdminSearchResult;
import or.sopt.houme.compare.application.GeminiPromptService;
import or.sopt.houme.compare.presentation.dto.request.AdminImageSearchRequest;
import or.sopt.houme.compare.presentation.dto.request.AdminTextSearchRequest;
import or.sopt.houme.compare.presentation.dto.request.GeminiPromptRequest;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Tag(name = "Admin 가격비교 API")
@RequestMapping("/api/admin/v1/compare")
public class AdminCompareController {

    private final AdminEbaySearchService adminEbaySearchService;
    private final GeminiPromptService geminiPromptService;

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

    @Operation(
            summary = "[TEMP] Gemini 프롬프트 실행",
            description = "입력한 프롬프트를 gemini-3.5-flash-lite에 전달하고 텍스트 응답을 반환합니다."
    )
    @PostMapping("/gemini/prompt")
    public ResponseEntity<ApiResponse<String>> generateGeminiPrompt(
            @Valid @RequestBody GeminiPromptRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(geminiPromptService.generate(request.prompt())));
    }
}
