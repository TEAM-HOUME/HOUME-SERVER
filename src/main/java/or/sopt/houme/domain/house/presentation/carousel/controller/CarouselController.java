package or.sopt.houme.domain.house.presentation.carousel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.house.service.carousel.facade.CarouselOptimisticLockFacade;
import or.sopt.houme.domain.house.service.carousel.CarouselService;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CarouselException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "캐러셀 관련 API")
public class CarouselController {

    private final CarouselService carouselService;
    private final CarouselOptimisticLockFacade carouselOptimisticLockFacade;

    @GetMapping("/api/v1/carousels")
    @Operation(summary = "캐러셀 조회 API",
    description = "한 번 조회 시, 다섯개의 캐러셀을 반환합니다. <br><br>" +
            "**page는 0부터** 넣어주세요 (null일시 0이 기본)")
    public ResponseEntity<ApiResponse<GetCarouselListResponseDTO>> getCarousels(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {

        GetCarouselListResponseDTO carousels = carouselService.getCarousel(page);

        return ResponseEntity.ok(ApiResponse.ok(carousels));
    }

    @GetMapping("/api/v2/carousels")
    @Operation(summary = "캐러셀 조회 API v2",
            description = "실제 상품을 응답합니다. 한 번 조회 시, 다섯개의 캐러셀을 반환합니다. <br><br>" +
                    "**page는 0부터** 넣어주세요 (null일시 0이 기본)")
    public ResponseEntity<ApiResponse<GetCarouselListResponseDTO>> getCarouselsV2(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {

        GetCarouselListResponseDTO carousels = carouselService.getCarouselV2(page);

        return ResponseEntity.ok(ApiResponse.ok(carousels));
    }


    @PostMapping("/api/v1/carousels/like")
    @Operation(summary = "캐러셀 좋아요 API")
    public ResponseEntity<ApiResponse<String>> likeCarousel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long carouselId) {

        try {
            carouselOptimisticLockFacade.likeCarousel(userDetails.getUser(), carouselId);
        } catch (InterruptedException e) {
            throw new CarouselException(ErrorCode.CAROUSEL_INTERRUPT_EXCEPTION);
        }

        return ResponseEntity.ok(ApiResponse.ok("캐러셀 좋아요가 정상적으로 저장되었습니다"));
    }


    @PostMapping("/api/v1/carousels/hate")
    @Operation(summary = "캐러셀 싫어요 API")
    public ResponseEntity<ApiResponse<String>> hateCarousel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long carouselId) {

        try {
            carouselOptimisticLockFacade.hateCarousel(userDetails.getUser(), carouselId);
        }catch (InterruptedException e) {
            throw new CarouselException(ErrorCode.CAROUSEL_INTERRUPT_EXCEPTION);
        }

        return ResponseEntity.ok(ApiResponse.ok("캐러셀 싫어요가 정상적으로 저장되었습니다"));
    }
}
