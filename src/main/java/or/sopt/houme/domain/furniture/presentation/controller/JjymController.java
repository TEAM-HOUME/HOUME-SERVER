package or.sopt.houme.domain.furniture.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymToggleResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymV2ListResponse;
import or.sopt.houme.domain.furniture.service.facade.JjymOptimisticLockFacade;
import or.sopt.houme.domain.furniture.service.JjymService;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "찜 관련 API")
public class JjymController {


    private final JjymService jjymService;
    private final JjymOptimisticLockFacade jjymOptimisticLockFacade;


    @Operation(summary = "추천 가구 찜 토글 API", description = "이미 찜이면 해제, 아니면 찜으로 저장합니다")
    @PostMapping("/api/v1/recommend-furnitures/{recommendFurnitureId}/jjym")
    public ResponseEntity<ApiResponse<JjymToggleResponse>> toggleJjym(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recommendFurnitureId
    ) {
        boolean favorited = jjymOptimisticLockFacade.toggle(userDetails.getUser(), recommendFurnitureId);
        return ResponseEntity.ok(ApiResponse.ok(new JjymToggleResponse(favorited)));
    }


    @Operation(summary = "내가 찜한 가구 목록 조회 API",
            description = "찜한 가구의 이미지, 이름, 가구 식별자를 반환합니다.")
    @GetMapping("/api/v1/jjyms")
    public ResponseEntity<ApiResponse<JjymListResponse>> getMyJjyms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        JjymListResponse response = jjymService.getMyJjyms(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "원천 상품 찜 토글 API v2", description = "curation_raw_product 기준으로 찜을 등록/해제합니다.")
    @PostMapping("/api/v2/curation-raw-products/{rawProductId}/jjym")
    public ResponseEntity<ApiResponse<JjymToggleResponse>> toggleRawProductJjym(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long rawProductId
    ) {
        boolean favorited = jjymOptimisticLockFacade.toggleRawProduct(userDetails.getUser(), rawProductId);
        return ResponseEntity.ok(ApiResponse.ok(new JjymToggleResponse(favorited)));
    }

    @Operation(summary = "내가 찜한 원천 상품 목록 조회 API v2", description = "찜한 원천 상품의 색상, 브랜드, 가격, 찜 개수를 포함해 반환합니다.")
    @GetMapping("/api/v2/jjyms")
    public ResponseEntity<ApiResponse<JjymV2ListResponse>> getMyRawProductJjyms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        JjymV2ListResponse response = jjymService.getMyRawProductJjyms(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
