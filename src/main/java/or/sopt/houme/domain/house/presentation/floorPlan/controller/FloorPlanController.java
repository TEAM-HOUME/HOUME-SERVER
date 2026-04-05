package or.sopt.houme.domain.house.presentation.floorPlan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateDetailResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.RecentFloorPlanResponse;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.house.service.floorPlan.FloorPlanService;
import or.sopt.houme.domain.house.service.floorPlan.facade.FloorPlanFacade;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "집 구조 API")
@RequiredArgsConstructor
public class FloorPlanController {

    private final FloorPlanFacade floorPlanFacade;
    private final FloorPlanService floorPlanService;

    // 구조에 따른 도면 템플릿 제공
    @Operation(summary = "도면 템플릿 제공 API",
            description = "사용자가 가장 최근에 입력한 집 구조에 따른 도면 템플릿을 제공합니다.")
    @GetMapping("/api/v1/house-templates")
    public ResponseEntity<ApiResponse<FloorPlanListResponse>> getHouseTemplates(@AuthenticationPrincipal CustomUserDetails userDetails) {

        FloorPlanListResponse floorPlan = floorPlanFacade.getFloorPlan(userDetails.getUser());
        return ResponseEntity.ok().body(ApiResponse.ok(floorPlan));
    }

    @GetMapping("/api/v2/house-templates")
    @Operation(summary = "도면 전체 조회 API")
    public ResponseEntity<ApiResponse<ExploreHouseTemplateListResponse>> getExploreHouseTemplates(
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Form residenceType,
            @RequestParam(required = false) Structure layoutType,
            @RequestParam(required = false) Equilibrium equilibrium,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                floorPlanService.getExploreHouseTemplates(
                        size,
                        residenceType,
                        layoutType,
                        equilibrium,
                        userDetails != null ? userDetails.getUser() : null
                )
        ));
    }

    @GetMapping("/api/v2/house-templates/{floorPlanId}")
    @Operation(summary = "도면 상세 조회 API")
    public ResponseEntity<ApiResponse<ExploreHouseTemplateDetailResponse>> getExploreHouseTemplateDetail(
            @PathVariable Long floorPlanId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(floorPlanService.getExploreHouseTemplateDetail(floorPlanId)));
    }

    @GetMapping("/api/v2/recent-floor-plan")
    @Operation(summary = "최근 사용한 도면 데이터 조회 API")
    public ResponseEntity<ApiResponse<RecentFloorPlanResponse>> getRecentFloorPlan(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                floorPlanService.getRecentFloorPlan(userDetails != null ? userDetails.getUser() : null)
        ));
    }
}
