package or.sopt.houme.domain.furniture.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.service.CurationProductService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curations/products")
@RequiredArgsConstructor
@Tag(name = "상품 큐레이션 API")
public class CurationProductController {

    private final CurationProductService curationProductService;

    @GetMapping("/filters")
    @Operation(summary = "상품 검색 필터링 조건 조회 API", 
               description = "상품 탭에서 검색 시 사용되는 가구 유형, 가격대, 색상 필터 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<CurationProductFilterResponse>> getFilters() {
        return ResponseEntity.ok(ApiResponse.ok(curationProductService.getFilterMetadata()));
    }
}
