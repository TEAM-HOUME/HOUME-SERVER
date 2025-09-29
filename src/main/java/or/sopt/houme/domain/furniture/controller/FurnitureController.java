package or.sopt.houme.domain.furniture.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.dto.request.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.service.FurnitureService;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FurnitureController {

    private final FurnitureService furnitureService;

    // 주요활동, 가구들 제공 API
    @Operation(summary = "주요 활동, 가구 리스트 제공 API",
            description = "- 주요 활동 (휴식형, 재택근무형, 영화 감상형, 홈카페형)\n" +
                    "- 가구\n" +
                    "    - 침대 (싱글, 슈퍼싱글, 더블, 퀸 이상)\n" +
                    "- 그 외\n" +
                    "    - 책상\n" +
                    "    - 이동식 TV\n" +
                    "    - 서랍장\n" +
                    "    - 식탁, 의자\n" +
                    "    - 옷장\n" +
                    "    - 소파")
    @GetMapping("/dashboard-info")
    public ResponseEntity<ApiResponse<FurnitureAndActivityResponse>> getFurnitureAndActivity() {

        FurnitureAndActivityResponse furnitureAndActivity = furnitureService.getFurnitureAndActivity();

        return ResponseEntity.ok(ApiResponse.ok(furnitureAndActivity));
    }

    @Operation(summary = "생성된 이미지에서 가구 카테고리 조회 API",
            description = "생성된 이미지에서 소파, 침대, 스탠드, 러그와 같은 카테고리를 제공합니다.\n" +
                    "각 가구 카테고리의 순서는 스타일에 따라 다릅니다.")
    @GetMapping("/generated-images/{imageId}/curations/categories")
    public ResponseEntity<ApiResponse<FurnitureCategoriesResponse>> getFurnitureCategories(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long imageId, @RequestParam List<String> detectedObjects) {
        FurnitureCategoriesResponse response = furnitureService.getFurnitureCategoriesByStyle(userDetails.getUser(), imageId, detectedObjects);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "가구 카테고리를 클릭하여 가구 제품 조회 API",
            description = "생성된 이미지의 큐레이션 탭에서 가구 카테고리를 클릭하여 네이버 쇼핑 API를 통한 가구 제품들을 검색합니다.")
    @GetMapping("/api/v1/generated-images/{imageId}/curations/products/{categoryId}")
    public ResponseEntity<ApiResponse<FurnitureProductsInfoResponse>> getFurnitureProductInfoFromNaverApi(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long imageId, Long categoryId) {
        FurnitureProductsInfoResponse response = furnitureService.getFurnitureProductInfoFromNaverApi(userDetails.getUser(), imageId, categoryId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
