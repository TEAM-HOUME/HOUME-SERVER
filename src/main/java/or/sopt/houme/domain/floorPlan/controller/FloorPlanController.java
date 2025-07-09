package or.sopt.houme.domain.floorPlan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.floorPlan.facade.FloorPlanFacade;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "집 구조 API")
@RequiredArgsConstructor
public class FloorPlanController {

    private final FloorPlanFacade floorPlanFacade;

    // 구조에 따른 도면 템플릿 제공
    @Operation(summary = "도면 템플릿 제공 API",
            description = "입력받은 구조에 따른 도면 템플릿을 제공합니다. 가장 최근 입력한 house 테이블")
    @GetMapping("/house-templates")
    public ResponseEntity<ApiResponse<FloorPlanListResponse>> getHouseTemplates(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok().body(ApiResponse.ok(floorPlanFacade.getFloorPlan(userDetails.getUser())));
    }
}
