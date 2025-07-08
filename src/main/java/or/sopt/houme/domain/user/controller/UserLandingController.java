package or.sopt.houme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.service.UserLandingService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "회원 랜딩페이지 관련 API")
public class UserLandingController {

    private final UserLandingService userLandingService;

    @GetMapping("/check-has-generated-image")
    @Operation(summary = "회원 이미지 생성 이력 조회 API",
    description = "회원의 리프레시 토큰의 유효성과 이미지 생성 이력을 조회합니다 <br><br>" +
            "이미지 생성 이력이 없으면 **false** 있다면 **true** 를 반환합니다")
    public ResponseEntity<ApiResponse<Boolean>> checkHasGeneratedImage(HttpServletRequest request) {

        Boolean result = userLandingService.getHasGeneratedImage(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
