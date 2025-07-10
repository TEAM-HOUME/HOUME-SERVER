package or.sopt.houme.domain.carousel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.carousel.facade.CarouselOptimisticLockFacade;
import or.sopt.houme.domain.carousel.service.CarouselService;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "캐러셀 관련 API")
public class CarouselController {

    private final CarouselService carouselService;
    private final CarouselOptimisticLockFacade carouselOptimisticLockFacade;

    @GetMapping("/carousels")
    @Operation(summary = "캐러셀 조회 API",
    description = "한 번 조회 시, 다섯개의 캐러셀을 반환합니다. <br><br>" +
            "**page는 0부터** 넣어주세요")
    public ResponseEntity<ApiResponse<GetCarouselListResponseDTO>> getCarousels(
            @RequestParam Integer page) {

        GetCarouselListResponseDTO carousels = carouselService.getCarousel(page);

        return ResponseEntity.ok(ApiResponse.ok(carousels));
    }


    @PostMapping("/carousels/like")
    @Operation(summary = "캐러셀 좋아요 API")
    public ResponseEntity<ApiResponse<String>> likeCarousel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long carouselId) throws InterruptedException {

        carouselOptimisticLockFacade.likeCarousel(userDetails.getUser(), carouselId);

        return ResponseEntity.ok(ApiResponse.ok("캐러셀 좋아요가 정상적으로 저장되었습니다"));
    }


    @PostMapping("/carousels/hate")
    @Operation(summary = "캐러셀 싫어요 API")
    public ResponseEntity<ApiResponse<String>> hateCarousel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long carouselId) throws InterruptedException {

        carouselOptimisticLockFacade.hateCarousel(userDetails.getUser(), carouselId);

        return ResponseEntity.ok(ApiResponse.ok("캐러셀 싫어요가 정상적으로 저장되었습니다"));
    }
}
