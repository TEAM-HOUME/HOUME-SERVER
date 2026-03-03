package or.sopt.houme.domain.furniture.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductResponse;
import or.sopt.houme.domain.furniture.service.admin.AdminCurationRawProductService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/curation-raw-products")
@Tag(name = "어드민 큐레이션 원본 상품 API")
public class AdminCurationRawProductController {

    private final AdminCurationRawProductService adminCurationRawProductService;

    @GetMapping
    @Operation(summary = "curation_raw_product 전체 조회 API")
    public ResponseEntity<ApiResponse<AdminCurationRawProductListResponse>> getRawProducts() {
        return ResponseEntity.ok(ApiResponse.ok(adminCurationRawProductService.getAll()));
    }

    @GetMapping("/{curationRawProductId}")
    @Operation(summary = "curation_raw_product 개별 제품 상세조회 API")
    public ResponseEntity<ApiResponse<AdminCurationRawProductResponse>> getRawProduct(
            @PathVariable Long curationRawProductId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminCurationRawProductService.getById(curationRawProductId)));
    }

    @PostMapping
    @Operation(summary = "curation_raw_product 가구 추가 API")
    public ResponseEntity<ApiResponse<AdminCurationRawProductResponse>> createRawProduct(
            @Valid @RequestBody AdminCurationRawProductCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminCurationRawProductService.create(request)));
    }

    @PatchMapping("/{curationRawProductId}")
    @Operation(summary = "curation_raw_product 가구 수정 API")
    public ResponseEntity<ApiResponse<AdminCurationRawProductResponse>> updateRawProduct(
            @PathVariable Long curationRawProductId,
            @RequestBody AdminCurationRawProductUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminCurationRawProductService.update(curationRawProductId, request)));
    }

    @DeleteMapping("/{curationRawProductId}")
    @Operation(summary = "curation_raw_product 가구 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteRawProduct(
            @PathVariable Long curationRawProductId
    ) {
        adminCurationRawProductService.delete(curationRawProductId);
        return ResponseEntity.ok(ApiResponse.ok("큐레이션 원본 상품이 삭제되었습니다."));
    }
}
