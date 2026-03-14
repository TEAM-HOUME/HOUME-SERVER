package or.sopt.houme.domain.user.presentation.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerResponse;
import or.sopt.houme.domain.user.service.admin.AdminBannerService;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/banners")
@Tag(name = "어드민 배너 API")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminBannerController {

    private static final int MAX_RAW_PRODUCT_SEARCH_SIZE = 50;

    private final AdminBannerService adminBannerService;

    @PostMapping("/image-upload-url")
    @Operation(summary = "배너 이미지 업로드용 presigned url 생성 API")
    public ResponseEntity<ApiResponse<AdminBannerImageUploadResponse>> createBannerImageUploadUrl(
            @Valid @RequestBody AdminBannerImageUploadRequest request,
            @RequestParam("contentType") @NotBlank String contentType
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminBannerService.createImageUploadUrl(request, contentType)));
    }

    @PostMapping
    @Operation(summary = "배너 생성 API")
    public ResponseEntity<ApiResponse<AdminBannerResponse>> createBanner(
            @Valid @RequestBody AdminBannerCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminBannerService.create(request)));
    }

    @GetMapping
    @Operation(summary = "배너 목록 조회 API")
    public ResponseEntity<ApiResponse<AdminBannerListResponse>> getBanners() {
        return ResponseEntity.ok(ApiResponse.ok(adminBannerService.getAll()));
    }

    @GetMapping("/{bannerId}")
    @Operation(summary = "배너 상세 조회 API")
    public ResponseEntity<ApiResponse<AdminBannerResponse>> getBanner(@PathVariable Long bannerId) {
        return ResponseEntity.ok(ApiResponse.ok(adminBannerService.getById(bannerId)));
    }

    @PatchMapping("/{bannerId}")
    @Operation(summary = "배너 수정 API")
    public ResponseEntity<ApiResponse<AdminBannerResponse>> updateBanner(
            @PathVariable Long bannerId,
            @Valid @RequestBody AdminBannerUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminBannerService.update(bannerId, request)));
    }

    @DeleteMapping("/{bannerId}")
    @Operation(summary = "스타일 배너 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteBanner(@PathVariable Long bannerId) {
        adminBannerService.delete(bannerId);
        return ResponseEntity.ok(ApiResponse.ok("배너가 삭제되었습니다."));
    }

    @GetMapping("/raw-products/search")
    @Operation(summary = "배너용 RAW 상품 검색 API")
    public ResponseEntity<ApiResponse<AdminBannerRawProductSearchResponse>> searchRawProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        validateSearchSize(size);
        return ResponseEntity.ok(ApiResponse.ok(adminBannerService.searchRawProducts(keyword, size)));
    }

    private void validateSearchSize(int size) {
        if (size < 1 || size > MAX_RAW_PRODUCT_SEARCH_SIZE) {
            throw new GeneralException(ErrorCode.NOT_VALID_EXCEPTION);
        }
    }
}
