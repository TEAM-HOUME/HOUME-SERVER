package or.sopt.houme.domain.generateImageResult.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.generateImageResult.service.GenerateImageResultService;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/generated-images")
@Tag(name = "이미지 생성 결과 API")
public class GenerateImageResultController {

    private final GenerateImageResultService generateImageResultService;

    @Operation(summary = "이미지 & 선택한 아이템 조회 API",
            description = "generation_type이 LIST인 생성 이미지에 대해 결과 이미지와 연결된 상품 목록을 조회합니다.")
    @GetMapping("/list-result/{imageId}/items")
    public ResponseEntity<ApiResponse<GenerateImageResultResponse>> getListResultItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long imageId
    ) {
        GenerateImageResultResponse response =
                generateImageResultService.getListResultItems(userDetails.getUser(), imageId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
