package or.sopt.houme.domain.user.presentation.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response.AdminLandingListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response.AdminLandingResponse;
import or.sopt.houme.domain.user.service.admin.AdminLandingService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/admin/landings")
@Tag(name = "어드민 랜딩 API")
@Validated
public class AdminLandingController {

    private final AdminLandingService adminLandingService;

    @PostMapping("/image-upload-url")
    @Operation(summary = "랜딩 이미지 업로드용 presigned url 생성 API")
    public ResponseEntity<ApiResponse<AdminBannerImageUploadResponse>> createLandingImageUploadUrl(
            @Valid @RequestBody AdminBannerImageUploadRequest request,
            @RequestParam("contentType") @NotBlank String contentType
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminLandingService.createImageUploadUrl(request, contentType)));
    }

    @PostMapping
    @Operation(summary = "랜딩 생성 API")
    public ResponseEntity<ApiResponse<AdminLandingResponse>> createLanding(
            @Valid @RequestBody AdminLandingCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminLandingService.create(request)));
    }

    @GetMapping
    @Operation(summary = "랜딩 목록 조회 API")
    public ResponseEntity<ApiResponse<AdminLandingListResponse>> getLandings() {
        return ResponseEntity.ok(ApiResponse.ok(adminLandingService.getAll()));
    }

    @GetMapping("/{landingId}")
    @Operation(summary = "랜딩 상세 조회 API")
    public ResponseEntity<ApiResponse<AdminLandingResponse>> getLanding(@PathVariable Long landingId) {
        return ResponseEntity.ok(ApiResponse.ok(adminLandingService.getById(landingId)));
    }

    @PatchMapping("/{landingId}")
    @Operation(summary = "랜딩 수정 API")
    public ResponseEntity<ApiResponse<AdminLandingResponse>> updateLanding(
            @PathVariable Long landingId,
            @Valid @RequestBody AdminLandingUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminLandingService.update(landingId, request)));
    }

    @DeleteMapping("/{landingId}")
    @Operation(summary = "랜딩 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteLanding(@PathVariable Long landingId) {
        adminLandingService.delete(landingId);
        return ResponseEntity.ok(ApiResponse.ok("랜딩이 삭제되었습니다."));
    }
}
