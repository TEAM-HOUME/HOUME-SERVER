package or.sopt.houme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.service.OAuthService;
import or.sopt.houme.global.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "소셜로그인 관련 API")
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/oauth/kakao")
    @Operation(summary = "카카오 소셜로그인 API",
            description = "카카오 인증서버로 리다이렉트합니다. <br><br>" +
                    "요청의 Origin(예: http://localhost:5173, https://www.houme.kr)을 기반으로 동적으로 redirect_uri를 계산합니다. <br><br>" +
                    "프론트에서 인가코드(code)를 파싱하여 아래 콜백 API로 전달하세요.")
    public void kakaoOAuthCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String redirectAddress = oAuthService.requestRedirect(request);
        response.sendRedirect(redirectAddress);
    }


    @Operation(summary = "카카오 인증서버 토큰 검증 API",
    description = "프론트에서 전달한 code를 이용해 토큰 교환 및 로그인 처리를 수행합니다. <br><br>" +
            "액세스 토큰은 헤더에, 리프레시 토큰은 쿠키에 담아 반환합니다.")
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<Boolean>> kakaoLogin(@RequestParam("code") String accessCode, HttpServletRequest request, HttpServletResponse response) {

        Boolean result = oAuthService.kakaoLogin(accessCode, request, response);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "로그아웃 API")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {

        oAuthService.logout(userDetails,request,response);

        return ResponseEntity.ok(ApiResponse.ok("로그아웃이 정상적으로 처리되었습니다"));
    }
}
