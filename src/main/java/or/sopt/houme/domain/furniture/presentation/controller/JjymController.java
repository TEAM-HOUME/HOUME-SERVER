package or.sopt.houme.domain.furniture.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.JjymToggleResponse;
import or.sopt.houme.domain.furniture.service.facade.JjymOptimisticLockFacade;
import or.sopt.houme.domain.furniture.service.JjymService;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "찜 관련 API")
public class JjymController {


    private final JjymService jjymService;
    private final JjymOptimisticLockFacade jjymOptimisticLockFacade;


    @Operation(summary = "추천 가구 찜 토글 API", description = "이미 찜이면 해제, 아니면 찜으로 저장합니다")
    @PostMapping("/recommend-furnitures/{recommendFurnitureId}/jjym")
    public ResponseEntity<ApiResponse<JjymToggleResponse>> toggleJjym(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recommendFurnitureId
    ) {
        boolean favorited = jjymOptimisticLockFacade.toggle(userDetails.getUser(), recommendFurnitureId);
        return ResponseEntity.ok(ApiResponse.ok(new JjymToggleResponse(favorited)));
    }


    @Operation(summary = "내가 찜한 가구 목록 조회 API",
            description = "찜한 가구의 이미지, 이름, 가구 식별자를 반환합니다.")
    @GetMapping("/jjyms")
    public ResponseEntity<ApiResponse<JjymListResponse>> getMyJjyms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        JjymListResponse response = jjymService.getMyJjyms(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
