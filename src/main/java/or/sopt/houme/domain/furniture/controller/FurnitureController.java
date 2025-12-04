package or.sopt.houme.domain.furniture.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.dto.external.naverShop.FurnitureProductsInfoResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.dto.external.naverShop.forPlan.FurnitureProductsInfoResponseForPlan;
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
                    "각 가구 카테고리의 순서는 스타일에 따라 다릅니다.\n" +
                    "- 가구 영단어\n" +
                    "- 침대(모든 종류) : SINGLE\n" +
                    "- 업무용 책상 : OFFICE_DESK\n" +
                    "- 옷장 : CLOSET\n" +
                    "- 식탁 : DINING_TABLE\n" +
                    "- 1인용 소파 : SINGLE_SOFA\n" +
                    "- 수납장 : DRAWER\n" +
                    "- 이동식 TV : MOVABLE_TV\n" +
                    "- 좌식 테이블 : SITTING_TABLE\n" +
                    "- 전신 거울 : MIRROR\n" +
                    "- 책 선반 : WHITE_BOOKSHELF\n" +
                    "- 장식장 : DISPLAY_CABINET\n" +
                    "- 2인용 소파 : TWO_SEATER_SOFA")
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

    @Operation(summary = "[기획 의사결정용 API] 가구 카테고리를 클릭하여 가구 제품 조회 API",
            description = "- tagId와 furnitureId로 baseImage가 선택됩니다.\n" +
                    "  - 각 id에 대한 값은 노션으로 전달하겠습니다.\n" +
                    "- searchKeyword로 검색어를 커스텀할 수 있습니다.\n" +
                    "- pHash(0~100)사이값을 입력하여, pHash와 colorHash의 비율을 커스텀할 수 있습니다.\n" +
                    "  - colorHash는 100-pHash로 산정됩니다.")
    @GetMapping("/generated-images/{tagId}/curations/products/{furnitureId}/for-plan")
    public ResponseEntity<ApiResponse<FurnitureProductsInfoResponseForPlan>> getFurnitureProductInfoFromNaverApiForPlan(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tagId,
            @PathVariable Long furnitureId,
            @RequestParam String searchKeyword,
            @RequestParam int pHash
    ) {
        FurnitureProductsInfoResponseForPlan response = furnitureFacade.getFurnitureProductInfoFromNaverApiForPlan(
                userDetails.getUser(),
                tagId,
                furnitureId,
                searchKeyword,
                pHash
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "[기획 의사결정용 API V2] 가구 카테고리를 클릭하여 가구 제품 조회 API",
            description = "- tagId와 furnitureId로 baseImage가 선택됩니다.\n" +
                    "- searchKeyword로 검색어를 커스텀할 수 있습니다.\n" +
                    "- pHash(0~100)사이값을 입력하여, pHash와 colorHash의 비율을 커스텀할 수 있습니다.\n" +
                    "- V2: mallName/네이버페이 필터 파라미터 적용 (allowedMalls는 서버 프로퍼티 사용, payFilter는 빈 값)" )
    @GetMapping("/generated-images/{tagId}/curations/products/{furnitureId}/for-plan/detail")
    public ResponseEntity<ApiResponse<FurnitureProductsInfoResponseForPlan>> getFurnitureProductInfoFromNaverApiForPlanV2(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long tagId,
            @PathVariable Long furnitureId,
            @RequestParam String searchKeyword,
            @RequestParam int pHash,
            @RequestParam(required = false) List<String> allowedMalls,
            @RequestParam(required = false, defaultValue = "false") Boolean applyNaverPay
    ) {
        FurnitureProductsInfoResponseForPlan response = furnitureFacade.getFurnitureProductInfoFromNaverApiForPlanV2(
                userDetails.getUser(),
                tagId,
                furnitureId,
                searchKeyword,
                pHash,
                allowedMalls,
                applyNaverPay
        );

        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

