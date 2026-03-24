package or.sopt.houme.domain.explore.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.domain.explore.service.ExploreService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/explore")
@RequiredArgsConstructor
@Tag(name = "탐색 탭 관련 API")
public class ExploreController {

    private final ExploreService exploreService;

    @GetMapping("/banners/{bannerId}")
    @Operation(summary = "배너 전체 조회 API",
            description = "요청한 bannerId를 첫 번째로 두고 circular 방식으로 배너 목록을 반환합니다")
    public ResponseEntity<ApiResponse<BannerExploreListResponse>> getExploreBanners(@PathVariable Long bannerId) {
        return ResponseEntity.ok(ApiResponse.ok(exploreService.getExploreBanners(bannerId)));
    }
}
