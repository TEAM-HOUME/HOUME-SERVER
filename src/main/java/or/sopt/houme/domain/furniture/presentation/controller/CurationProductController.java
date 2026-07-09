package or.sopt.houme.domain.furniture.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductListResponse;
import or.sopt.houme.domain.furniture.service.CurationProductService;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/curations/products")
@RequiredArgsConstructor
@Tag(name = "상품 큐레이션 API")
@Validated
public class CurationProductController {

    private final CurationProductService curationProductService;

    @GetMapping
    @Operation(summary = "상품 리스트 조회 및 검색 API", 
               description = "상품 탭 메인 API입니다. 필터링 조건들을 포함하며 무한 스크롤을 지원합니다.")
    public ResponseEntity<ApiResponse<CurationProductListResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> types,
            @RequestParam(required = false) List<String> priceRanges,
            @RequestParam(required = false) List<Long> colors,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") @Positive @Max(100) Integer size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                curationProductService.getProducts(keyword, types, priceRanges, colors, cursor, size)
        ));
    }

    @GetMapping("/filters")
    @Operation(summary = "상품 검색 필터링 조건 조회 API", 
               description = "상품 탭에서 검색 시 사용되는 가구 유형, 가격대, 색상 필터 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<CurationProductFilterResponse>> getFilters() {
        return ResponseEntity.ok(ApiResponse.ok(curationProductService.getFilterMetadata()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회 API", 
               description = "상품의 상세 정보(이미지, 가격, 구매 링크 등)를 조회합니다.")
    public ResponseEntity<ApiResponse<CurationProductDetailResponse>> getProductDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails != null ? userDetails.getUser() : null;
        return ResponseEntity.ok(ApiResponse.ok(curationProductService.getProductDetail(id, user)));
    }
}
