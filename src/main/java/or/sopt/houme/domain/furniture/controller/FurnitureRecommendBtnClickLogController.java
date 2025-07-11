package or.sopt.houme.domain.furniture.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.service.FurnitureRecommendBtnClickLogService;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "가구 추천받기 버튼 클릭 로그 API")
@RequiredArgsConstructor
public class FurnitureRecommendBtnClickLogController {
    private final FurnitureRecommendBtnClickLogService furnitureRecommendBtnClickLogService;

    @PatchMapping(value = "/furnitures/logs")
    @Operation(summary = "가구 추천받기 버튼 클릭시 로그 저장 api")
    public ResponseEntity<ApiResponse<Void>> createFurnitureRecommendBtnClickLog(@AuthenticationPrincipal CustomUserDetails userDetails) {
        furnitureRecommendBtnClickLogService.createFurnitureRecommendBtnClickLog(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
