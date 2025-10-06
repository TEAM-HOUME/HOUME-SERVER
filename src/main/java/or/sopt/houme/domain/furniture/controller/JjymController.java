package or.sopt.houme.domain.furniture.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.dto.response.JjymToggleResponse;
import or.sopt.houme.domain.furniture.service.JjymService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.service.JjymService;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
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

    @Operation(summary = "추천 가구 찜 토글 API", description = "이미 찜이면 해제, 아니면 찜으로 저장합니다")
    @PostMapping("/recommend-furnitures/{recommendFurnitureId}/jjym")
    public ResponseEntity<ApiResponse<JjymToggleResponse>> toggleJjym(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long recommendFurnitureId
    ) {
        jjymService.jjymToggle(userDetails.getUser().getId(), recommendFurnitureId);
        return ResponseEntity.ok(ApiResponse.ok(
                new JjymToggleResponse()
        ));
    }


}
