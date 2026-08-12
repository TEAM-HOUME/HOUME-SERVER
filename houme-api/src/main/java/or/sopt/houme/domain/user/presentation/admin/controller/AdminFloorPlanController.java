package or.sopt.houme.domain.user.presentation.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request.AdminFloorPlanUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanListResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response.AdminFloorPlanResponse;
import or.sopt.houme.domain.user.service.admin.AdminFloorPlanService;
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
@RequestMapping("/api/v1/admin/floor-plans")
@Tag(name = "어드민 도면 API")
@Validated
public class AdminFloorPlanController {

    private final AdminFloorPlanService adminFloorPlanService;

    @PostMapping("/image-upload-url")
    @Operation(summary = "도면 이미지 업로드용 presigned url 생성 API")
    public ResponseEntity<ApiResponse<AdminFloorPlanImageUploadResponse>> createFloorPlanImageUploadUrl(
            @Valid @RequestBody AdminFloorPlanImageUploadRequest request,
            @RequestParam("contentType") @NotBlank String contentType
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminFloorPlanService.createImageUploadUrl(request, contentType)));
    }

    @PostMapping
    @Operation(summary = "도면 생성 API")
    public ResponseEntity<ApiResponse<AdminFloorPlanResponse>> createFloorPlan(
            @Valid @RequestBody AdminFloorPlanCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminFloorPlanService.create(request)));
    }

    @GetMapping
    @Operation(summary = "도면 목록 조회 API")
    public ResponseEntity<ApiResponse<AdminFloorPlanListResponse>> getFloorPlans() {
        return ResponseEntity.ok(ApiResponse.ok(adminFloorPlanService.getAll()));
    }

    @GetMapping("/{floorPlanId}")
    @Operation(summary = "도면 상세 조회 API")
    public ResponseEntity<ApiResponse<AdminFloorPlanResponse>> getFloorPlan(@PathVariable Long floorPlanId) {
        return ResponseEntity.ok(ApiResponse.ok(adminFloorPlanService.getById(floorPlanId)));
    }

    @PatchMapping("/{floorPlanId}")
    @Operation(summary = "도면 수정 API")
    public ResponseEntity<ApiResponse<AdminFloorPlanResponse>> updateFloorPlan(
            @PathVariable Long floorPlanId,
            @Valid @RequestBody AdminFloorPlanUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminFloorPlanService.update(floorPlanId, request)));
    }

    @DeleteMapping("/{floorPlanId}")
    @Operation(summary = "도면 삭제 API")
    public ResponseEntity<ApiResponse<String>> deleteFloorPlan(@PathVariable Long floorPlanId) {
        adminFloorPlanService.delete(floorPlanId);
        return ResponseEntity.ok(ApiResponse.ok("도면이 삭제되었습니다."));
    }
}
