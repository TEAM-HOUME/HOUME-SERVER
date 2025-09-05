package or.sopt.houme.domain.generateImage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.dto.request.GenerateImageRequest;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoListResponse;
import or.sopt.houme.domain.generateImage.dto.response.ImageInfoResponse;
import or.sopt.houme.domain.generateImage.facade.GenerateImageFacade;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.util.constant.S3Constant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "이미지 생성 API")
public class GenerateImageController {

    private final GenerateImageFacade generateImageFacade;

    @Operation(summary = "자바 스프링을 이용한 이미지 생성 API",
            description = "사용자가 요청한 내용을 기반으로 새로운 이미지를 생성합니다. 생성된 이미지는 저장되며, 별도의 조회 API를 통해 확인할 수 있습니다.")
    @PostMapping("/v1/generated-images/generate")
    public ResponseEntity<ApiResponse<ImageInfoResponse>> generateImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid GenerateImageRequest request){

        ImageInfoResponse imageInfoResponse = generateImageFacade.generateImage(userDetails.getUser(), request);

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


}
