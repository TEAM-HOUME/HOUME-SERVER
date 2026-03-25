package or.sopt.houme.domain.banner.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.presentation.dto.response.BannerDetailResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.LandingListResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.OtherStyleDetailResponse;
import or.sopt.houme.domain.banner.presentation.dto.response.OtherStyleListResponse;
import or.sopt.houme.domain.banner.service.BannerService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "배너 관련 API")
public class BannerController {

    private final BannerService bannerExploreService;

    @GetMapping("/landings")
    @Operation(summary = "랜딩 페이지 전체 조회 API")
    public ResponseEntity<ApiResponse<LandingListResponse>> getLandings() {
        return ResponseEntity.ok(ApiResponse.ok(bannerExploreService.getLandings()));
    }

    @GetMapping("/banners/{bannerId}")
    @Operation(summary = "배너 전체 조회 API",
            description = "요청한 bannerId를 첫 번째로 두고 circular 방식으로 배너 목록을 반환합니다")
    public ResponseEntity<ApiResponse<BannerExploreListResponse>> getExploreBanners(@PathVariable Long bannerId) {
        return ResponseEntity.ok(ApiResponse.ok(bannerExploreService.getExploreBanners(bannerId)));
    }

    @GetMapping("/banners/{bannerId}/detail")
    @Operation(summary = "배너 디테일 페이지 조회 API")
    public ResponseEntity<ApiResponse<BannerDetailResponse>> getExploreBannerDetail(@PathVariable Long bannerId) {
        return ResponseEntity.ok(ApiResponse.ok(bannerExploreService.getExploreBannerDetail(bannerId)));
    }

    @GetMapping("/other-styles")
    @Operation(summary = "다른 스타일 전체 조회 API")
    public ResponseEntity<ApiResponse<OtherStyleListResponse>> getOtherStyles(
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(bannerExploreService.getOtherStyles(size)));
    }

    @GetMapping("/other-styles/{styleId}")
    @Operation(summary = "스타일 디테일 페이지 조회 API")
    public ResponseEntity<ApiResponse<OtherStyleDetailResponse>> getOtherStyleDetail(@PathVariable Long styleId) {
        return ResponseEntity.ok(ApiResponse.ok(bannerExploreService.getOtherStyleDetail(styleId)));
    }
}
