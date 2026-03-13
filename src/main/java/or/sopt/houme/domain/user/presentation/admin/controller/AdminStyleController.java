package or.sopt.houme.domain.user.presentation.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response.AdminStyleListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response.AdminStyleResponse;
import or.sopt.houme.domain.user.service.admin.AdminStyleService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/admin/styles")
@Tag(name = "어드민 스타일 API")
public class AdminStyleController {

    private final AdminStyleService adminStyleService;

    @PostMapping("/image-upload-url")
    @Operation(summary = "스타일 이미지 업로드용 presigned url 생성 API")
    public ResponseEntity<ApiResponse<AdminBannerImageUploadResponse>> createStyleImageUploadUrl(
            @Valid @RequestBody AdminBannerImageUploadRequest request,
            @RequestParam("contentType") String contentType
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminStyleService.createImageUploadUrl(request, contentType)));
    }

    @PostMapping
    @Operation(summary = "스타일 생성 API")
    public ResponseEntity<ApiResponse<AdminStyleResponse>> createStyle(
            @Valid @RequestBody AdminStyleCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminStyleService.create(request)));
    }

    @GetMapping
    @Operation(summary = "스타일 목록 조회 API")
    public ResponseEntity<ApiResponse<AdminStyleListResponse>> getStyles() {
        return ResponseEntity.ok(ApiResponse.ok(adminStyleService.getAll()));
    }

    @GetMapping("/{styleId}")
    @Operation(summary = "스타일 상세 조회 API")
    public ResponseEntity<ApiResponse<AdminStyleResponse>> getStyle(@PathVariable Long styleId) {
        return ResponseEntity.ok(ApiResponse.ok(adminStyleService.getById(styleId)));
    }

    @PatchMapping("/{styleId}")
    @Operation(summary = "스타일 수정 API")
    public ResponseEntity<ApiResponse<AdminStyleResponse>> updateStyle(
            @PathVariable Long styleId,
            @Valid @RequestBody AdminStyleUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminStyleService.update(styleId, request)));
    }

    @DeleteMapping("/{styleId}")
    @Operation(summary = "스타일 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteStyle(@PathVariable Long styleId) {
        adminStyleService.delete(styleId);
        return ResponseEntity.ok(ApiResponse.ok("스타일이 삭제되었습니다."));
    }

    @GetMapping("/raw-products/search")
    @Operation(summary = "스타일용 RAW 상품 검색 API")
    public ResponseEntity<ApiResponse<AdminBannerRawProductSearchResponse>> searchRawProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminStyleService.searchRawProducts(keyword, size)));
    }
}
