package or.sopt.houme.domain.generateImage.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.presentation.dto.request.BannerGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.request.GenerateImageV4Request;
import or.sopt.houme.domain.generateImage.presentation.dto.request.OtherStyleGenerateImageRequest;
import or.sopt.houme.domain.generateImage.presentation.dto.response.BannerGenerateImageResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.GenerateImageV4Response;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoListResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.presentation.dto.response.OtherStyleGenerateImageResponse;
import or.sopt.houme.domain.generateImage.service.facade.GenerateImageFacade;
import or.sopt.houme.domain.generateImage.service.facade.GenerateImageLikeFacade;
import or.sopt.houme.domain.house.presentation.dto.request.IsLikeRequest;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.GenerateImageException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "이미지 생성 API")
public class GenerateImageController {

    private final GenerateImageFacade generateImageFacade;
    private final GenerateImageLikeFacade generateImageLikeFacade;

    @Operation(summary = "자바 스프링을 이용한 이미지 생성 API",
            description = "사용자가 요청한 내용을 기반으로 새로운 이미지를 생성합니다. 생성된 이미지는 저장되며, 별도의 조회 API를 통해 확인할 수 있습니다.")
    @PostMapping("/v1/generated-images/generate")
    public ResponseEntity<ApiResponse<ImageInfoResponse>> generateImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid GenerateImageRequest request){

        ImageInfoResponse imageInfoResponse = generateImageFacade.generateImage(userDetails.getUser(), request);

        return ResponseEntity.ok(ApiResponse.ok(imageInfoResponse));
    }

    @Operation(summary = "Gemini 이미지 생성 API",
            description = "사용자가 요청한 내용을 기반으로 새로운 이미지를 생성합니다. 생성된 이미지는 저장되며, 별도의 조회 API를 통해 확인할 수 있습니다.")
    @PostMapping("/v1/generated-images/generate/gemini")
    public ResponseEntity<ApiResponse<ImageInfoResponse>> generateImageByGemini(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid GenerateImageRequest request){

        ImageInfoResponse imageInfoResponse = generateImageFacade.generateImageByGemini(userDetails.getUser(), request);

        return ResponseEntity.ok(ApiResponse.ok(imageInfoResponse));
    }

    @Operation(summary = "LangChain를 이용한 이미지 생성 API",
            description = "사용자가 요청한 내용을 기반으로 새로운 이미지를 생성합니다. 생성된 이미지는 저장되며, 별도의 조회 API를 통해 확인할 수 있습니다. <br><br>" +
                    "FastAPI를 활용한 API 입니다")
    @PostMapping("/v2/generated-images/generate")
    public ResponseEntity<ApiResponse<ImageInfoResponse>> generateImageByFastAPI(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid GenerateImageRequest request){

        ImageInfoResponse imageInfoResponse = generateImageFacade.generateImageByFastApi(userDetails.getUser(), request);

        return ResponseEntity.ok(ApiResponse.ok(imageInfoResponse));
    }

    @Operation(summary = "Gemini 이미지 생성 API (FastAPI 대체)",
            description = "사용자가 요청한 내용을 기반으로 새로운 이미지를 생성합니다. 생성된 이미지는 저장되며, 별도의 조회 API를 통해 확인할 수 있습니다. <br><br>" +
                    "Gemini 모델을 활용한 API 입니다")
    @PostMapping("/v2/generated-images/generate/gemini")
    public ResponseEntity<ApiResponse<ImageInfoResponse>> generateImageByFastApiGemini(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid GenerateImageRequest request){

        ImageInfoResponse imageInfoResponse = generateImageFacade.generateImageByFastApiGemini(userDetails.getUser(), request);

        return ResponseEntity.ok(ApiResponse.ok(imageInfoResponse));
    }

    @Operation(summary = "비동기를 이용한 이미지 2장 생성 API",
            description = "사용자가 요청한 내용을 기반으로 높은 스타일 2개로 새로운 이미지 2장을 생성합니다. 생성된 이미지는 저장되며, 별도의 조회 API를 통해 확인할 수 있습니다. <br><br>" +
                    "FastAPI를 활용한 API 입니다")
    @PostMapping("/v3/generated-images/generate")
    public ResponseEntity<ApiResponse<ImageInfoListResponse>> generate2ImageByFastAPI(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid GenerateImageRequest request){

        ImageInfoListResponse imageInfoListResponse = generateImageFacade.generateImageBy2ea(userDetails.getUser(), request);

        return ResponseEntity.ok(ApiResponse.ok(imageInfoListResponse));
    }

    @Operation(summary = "Gemini 비동기 이미지 2장 생성 API",
            description = "사용자가 요청한 내용을 기반으로 높은 스타일 2개로 새로운 이미지 2장을 생성합니다. 생성된 이미지는 저장되며, 별도의 조회 API를 통해 확인할 수 있습니다. <br><br>" +
                    "Gemini 모델을 활용한 API 입니다")
    @PostMapping("/v3/generated-images/generate/gemini")
    public ResponseEntity<ApiResponse<ImageInfoListResponse>> generate2ImageByFastApiGemini(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid GenerateImageRequest request){

        ImageInfoListResponse imageInfoListResponse = generateImageFacade.generateImageBy2eaGemini(userDetails.getUser(), request);

        return ResponseEntity.ok(ApiResponse.ok(imageInfoListResponse));
    }

    @Operation(summary = "V4 이미지 생성 API",
            description = "도면/뷰, 무드보드, 주요활동, 가구를 기반으로 Gemini 모델로 이미지 1장을 생성합니다.")
    @PostMapping("/v4/generated-images/generate")
    public ResponseEntity<ApiResponse<GenerateImageV4Response>> generateImageV4ByGemini(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid GenerateImageV4Request request
    ) {
        GenerateImageV4Response response =
                generateImageFacade.generateImageV4ByGemini(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "배너 템플릿 기반 인테리어 이미지 생성 API",
            description = "선택한 배너 템플릿/답변칩/도면 정보를 기반으로 Gemini 나노바나나 모델로 인테리어 이미지를 생성합니다.")
    @PostMapping("/v1/generated-images/generate/banner")
    public ResponseEntity<ApiResponse<BannerGenerateImageResponse>> generateBannerImageByGemini(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BannerGenerateImageRequest request
    ) {
        BannerGenerateImageResponse response =
                generateImageFacade.generateBannerImageByGemini(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "선택한 스타일 템플릿 기반 인테리어 이미지 생성 API",
            description = "선택한 스타일 템플릿/도면 정보를 기반으로 Gemini 모델로 인테리어 이미지를 생성합니다.")
    @PostMapping("/v1/generated-images/generate/other-style")
    public ResponseEntity<ApiResponse<OtherStyleGenerateImageResponse>> generateOtherStyleImageByGemini(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid OtherStyleGenerateImageRequest request
    ) {
        OtherStyleGenerateImageResponse response =
                generateImageFacade.generateOtherStyleImageByGemini(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "이미지 생성 폴백 로직",
            description = "클라이언트의 새로고침 등 오류로 발생하게 되면 열리는 폴백 로직")
    @GetMapping("/v1/generated-images/generate")
    public ResponseEntity<ApiResponse<ImageInfoResponse>> getImageFallback(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long houseId
    ){
        ImageInfoResponse fallBackImage = generateImageFacade.getFallBackImage(userDetails.getUser(), houseId);

        return ResponseEntity.ok(ApiResponse.ok(fallBackImage));
    }

    @Operation(summary = "생성된 이미지 선호 여부 API",
            description = "생성된 이미지에 대한 선호도를 받습니다.")
    @PostMapping("/v1/generated-images/{imageId}/preference")
    public ResponseEntity<ApiResponse<Void>> generateImagePreference(
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid IsLikeRequest request
    ){
        try {
            generateImageLikeFacade.isLike(userDetails.getUser(), imageId, request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GenerateImageException(ErrorCode.GENERATE_IMAGE_INTERRUPT_EXCEPTION);
        }

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @Operation(summary = "이미지 선호 삭제 API",
            description = "생성된 이미지에 대한 선호도를 삭제합니다.")
    @DeleteMapping("/v1/generated-images/{imageId}/preference")
    public ResponseEntity<ApiResponse<Void>> deleteGenerateImagePreference(
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        generateImageLikeFacade.deletePreference(userDetails.getUser(), imageId);

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
