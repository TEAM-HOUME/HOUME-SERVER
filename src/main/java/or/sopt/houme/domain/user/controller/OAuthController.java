package or.sopt.houme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.controller.dto.KakaoLoginResponse;
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
                    "env 파라미터(local|dev)를 전달하면 해당 환경 기준으로 redirect_uri를 고정 생성합니다. <br>" +
                    "env 미전달 시 기존 로직(헤더 기반 추정)으로 동작합니다. <br><br>" +
                    "프론트에서 인가코드(code)를 파싱하여 아래 콜백 API로 전달하세요.")
    public void kakaoOAuthCallback(@RequestParam(value = "env", required = false) String env,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {

        String redirectAddress = (env == null)
                ? oAuthService.requestRedirect(request)
                : oAuthService.requestRedirect(request, env);
        response.sendRedirect(redirectAddress);
    }


    @Operation(summary = "카카오 인증서버 토큰 검증 API",
    description = "프론트에서 전달한 code를 이용해 토큰 교환 및 로그인 처리를 수행합니다. <br><br>" +
            "기존회원이면 액세스 토큰은 헤더에, 리프레시 토큰은 쿠키에 담아 반환합니다. <br>" +
            "신규회원이면 signupToken(임시토큰)과 prefill 정보를 반환합니다.")
    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<ApiResponse<KakaoLoginResponse>> kakaoLogin(@RequestParam("code") String accessCode,
                                                           @RequestParam(value = "env", required = false) String env,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {

        KakaoLoginResponse result = (env == null)
                ? oAuthService.kakaoLogin(accessCode, request, response)
                : oAuthService.kakaoLogin(accessCode, env, request, response);

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
