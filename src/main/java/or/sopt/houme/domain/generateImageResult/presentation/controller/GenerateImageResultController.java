package or.sopt.houme.domain.generateImageResult.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.GenerateImageResultResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.RelatedImagesResponse;
import or.sopt.houme.domain.generateImageResult.presentation.dto.response.SimilarItemsResponse;
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

    @Operation(summary = "[목록형 결과] 선택한 아이템 조회 API",
            description = "generation_type이 LIST인 생성 이미지에 대해 연결된 상품 목록을 조회합니다.")
    @GetMapping("/list-result/{imageId}/items")
    public ResponseEntity<ApiResponse<GenerateImageResultResponse>> getListResultItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long imageId
    ) {
        GenerateImageResultResponse response =
                generateImageResultService.getListResultItems(userDetails.getUser(), imageId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "[목록형 결과] 방금 담은 스타일과 비슷한 상품 조회 API",
            description = "generation_type이 LIST인 생성 이미지의 선택 상품을 기반으로 유사 상품을 최대 4개 조회합니다.")
    @GetMapping("/list-result/{imageId}/similar-items")
    public ResponseEntity<ApiResponse<SimilarItemsResponse>> getSimilarItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long imageId
    ) {
        SimilarItemsResponse response =
                generateImageResultService.getSimilarItems(userDetails.getUser(), imageId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "[목록형 결과] 00님이 고른 아이템이 포함된 이미지 조회 API",
            description = "요청 이미지의 선택 상품과 동일 상품이 포함된 다른 생성 이미지를 최신순으로 최대 10개 조회합니다.")
    @GetMapping("/list-result/{imageId}/related-images")
    public ResponseEntity<ApiResponse<RelatedImagesResponse>> getRelatedImages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long imageId
    ) {
        RelatedImagesResponse response =
                generateImageResultService.getRelatedImages(userDetails.getUser(), imageId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
