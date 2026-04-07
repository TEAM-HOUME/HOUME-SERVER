package or.sopt.houme.domain.furniture.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymToggleResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ListResponse;
import or.sopt.houme.domain.furniture.service.JjymService;
import or.sopt.houme.domain.furniture.service.facade.JjymOptimisticLockFacade;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
@Tag(name = "찜 관련 API v2")
public class JjymV2Controller {

    private final JjymService jjymService;
    private final JjymOptimisticLockFacade jjymOptimisticLockFacade;

    @Operation(summary = "원천 상품 찜 토글 API v2", description = "curation_raw_product 기준으로 찜을 등록/해제합니다.")
    @PostMapping("/curation-raw-products/{rawProductId}/jjym")
    public ResponseEntity<ApiResponse<JjymToggleResponse>> toggleRawProductJjym(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long rawProductId
    ) {
        boolean favorited = jjymOptimisticLockFacade.toggleRawProduct(userDetails.getUser(), rawProductId);
        return ResponseEntity.ok(ApiResponse.ok(new JjymToggleResponse(favorited)));
    }

    @Operation(summary = "내가 찜한 원천 상품 목록 조회 API v2", description = "찜한 원천 상품의 색상, 브랜드, 가격, 찜 개수를 포함해 반환합니다.")
    @GetMapping("/jjyms")
    public ResponseEntity<ApiResponse<JjymV2ListResponse>> getMyRawProductJjyms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        JjymV2ListResponse response = jjymService.getMyRawProductJjyms(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
