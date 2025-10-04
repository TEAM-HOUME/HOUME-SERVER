package or.sopt.houme.domain.furniture.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponseForPlan;
import or.sopt.houme.domain.furniture.facade.FurnitureFacade;
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
    private final FurnitureFacade furnitureFacade;

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
    @GetMapping("/generated-images/{imageId}/curations/products/{categoryId}")
    public ResponseEntity<ApiResponse<FurnitureProductsInfoResponse>> getFurnitureProductInfoFromNaverApi(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long imageId, @PathVariable Long categoryId) {
        FurnitureProductsInfoResponse response = furnitureFacade.getFurnitureProductInfoFromNaverApi(userDetails.getUser(), imageId, categoryId);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

//    @Operation(summary = "[기획 의사결정용 API] 가구 카테고리를 클릭하여 가구 제품 조회 API",
//            description = "검색어로 네이버 검색합니다." +
//                    "\n" +
//                    "\n사용법:" +
//                    "\n1. tagId와 furnitureId로 해시값을 계산할 가구 이미지를 선택합니다." +
//                    "\n2. 검색어는 그대로 사용하셔도 되고, 검색어 최적화를 원하신다면 다른 검색어로 검색하셔도 됩니다." +
//                    "\n3. searchProductsCount는 1~100만 가능합니다" +
//                    "\n" +
//                    "\ntagId:" +
//                    "\n1.natural_woody" +
//                    "\n2.romantic_pink" +
//                    "\n3.modern_white" +
//                    "\n4.vintage_green" +
//                    "\n5.vintage_pattern" +
//                    "\n6.midcentury_blue" +
//                    "\n" +
//                    "\nfurnitureId:" +
//                    "\n1.SINGLE 2.SUPER_SINGLE 3.DOUBLE 4.QUEEN_OVER 5.DESK" +
//                    "\n6.CLOSET 7.TABLE 8.ONE_SEATER_SOFA 9.DRAWER 10.MOVABLE_TV" +
//                    "\n11.TWO_SEATER_SOFA 12.SITTING_TABLE 13.FULLBODY_MIRROR 14.BOOK_SHELF 15.DISPLAY_CABINET")
//    @GetMapping("/generated-images/{tagId}/curations/products/forplan/{furnitureId}")
//    public ResponseEntity<ApiResponse<FurnitureProductsInfoResponseForPlan>> getFurnitureProductInfoFromNaverApiForPlan(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long tagId, @PathVariable Long furnitureId, @RequestParam String searchKeyword, int searchProductsCount) {
//        FurnitureProductsInfoResponseForPlan response = furnitureService.getFurnitureProductInfoFromNaverApiForPlan(userDetails.getUser(), tagId, furnitureId, searchKeyword, searchProductsCount);
//
//        return ResponseEntity.ok(ApiResponse.ok(response));
//    }
}
