package or.sopt.houme.domain.furniture.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductListResponse;
import or.sopt.houme.domain.furniture.service.CurationProductService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/curations/products")
@RequiredArgsConstructor
@Tag(name = "상품 큐레이션 API")
@Validated
public class CurationProductV2Controller {

    private final CurationProductService curationProductService;

    @GetMapping
    @Operation(
            summary = "[v2] 상품 리스트 조회 및 검색 API",
            description = "사전 토큰화 기반 검색 엔진을 적용한 상품 탭 메인 API입니다. " +
                          "v1 대비 상품명·브랜드명 외에 가구 유형명 및 어드민 설정 커스텀 키워드까지 검색됩니다."
    )
    public ResponseEntity<ApiResponse<CurationProductListResponse>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> types,
            @RequestParam(required = false) List<String> priceRanges,
            @RequestParam(required = false) List<Long> colors,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") @Positive @Max(100) Integer size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                curationProductService.getProductsV2(keyword, types, priceRanges, colors, cursor, size)
        ));
    }
}
