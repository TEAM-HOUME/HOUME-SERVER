package or.sopt.houme.domain.furniture.presentation.controller;

import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductExposureUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductFurnitureTagCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductFurnitureTagUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductColorOptionResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductFurnitureTagResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/curation-raw-products")
@Tag(name = "어드민 큐레이션 원본 상품 API")
public class AdminCurationRawProductController {

    private final AdminCurationRawProductService adminCurationRawProductService;

    @GetMapping
    @Operation(summary = "curation_raw_product 전체 조회 API")
    public ResponseEntity<ApiResponse<AdminCurationRawProductListResponse>> getRawProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SoozipCategory category,
            @RequestParam(required = false) Long minListPrice,
            @RequestParam(required = false) Long maxListPrice
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminCurationRawProductService.getAll(page, size, category, minListPrice, maxListPrice)
        ));
    }

    @GetMapping("/color-options")
    @Operation(summary = "curation_raw_product 색상 옵션 조회 API")
    public ResponseEntity<ApiResponse<List<AdminCurationRawProductColorOptionResponse>>> getColorOptions() {
        return ResponseEntity.ok(ApiResponse.ok(adminCurationRawProductService.getColorOptions()));
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
            @Valid @RequestBody AdminCurationRawProductUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminCurationRawProductService.update(curationRawProductId, request)));
    }

    @PatchMapping("/exposure")
    @Operation(summary = "curation_raw_product 노출 여부 일괄 수정 API")
    public ResponseEntity<ApiResponse<String>> updateRawProductExposure(
            @Valid @RequestBody AdminCurationRawProductExposureUpdateRequest request
    ) {
        adminCurationRawProductService.updateExposure(request);
        return ResponseEntity.ok(ApiResponse.ok("RAW 상품 노출 여부가 수정되었습니다."));
    }

    @PostMapping("/{curationRawProductId}/furniture-tags")
    @Operation(summary = "curation_raw_product 가구 태그 매핑 추가 API")
    public ResponseEntity<ApiResponse<AdminCurationRawProductFurnitureTagResponse>> createRawProductFurnitureTagMapping(
            @PathVariable Long curationRawProductId,
            @Valid @RequestBody AdminCurationRawProductFurnitureTagCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminCurationRawProductService.createFurnitureTagMapping(curationRawProductId, request)
        ));
    }

    @PatchMapping("/{curationRawProductId}/furniture-tags/{mappingId}")
    @Operation(summary = "curation_raw_product 가구 태그 매핑 수정 API")
    public ResponseEntity<ApiResponse<AdminCurationRawProductFurnitureTagResponse>> updateRawProductFurnitureTagMapping(
            @PathVariable Long curationRawProductId,
            @PathVariable Long mappingId,
            @Valid @RequestBody AdminCurationRawProductFurnitureTagUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminCurationRawProductService.updateFurnitureTagMapping(curationRawProductId, mappingId, request)
        ));
    }

    @DeleteMapping("/{curationRawProductId}/furniture-tags/{mappingId}")
    @Operation(summary = "curation_raw_product 가구 태그 매핑 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteRawProductFurnitureTagMapping(
            @PathVariable Long curationRawProductId,
            @PathVariable Long mappingId
    ) {
        adminCurationRawProductService.deleteFurnitureTagMapping(curationRawProductId, mappingId);
        return ResponseEntity.ok(ApiResponse.ok("RAW 상품 가구 태그 매핑이 삭제되었습니다."));
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
