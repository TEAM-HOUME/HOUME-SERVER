package or.sopt.houme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.service.OAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final OAuthService oAuthService;

    @GetMapping("/oauth/kakao")
    @Operation(summary = "카카오 소셜로그인 API",
            description = "카카오로 로그인 요청을 전송합니다. <br><br>" +
                    "카카오 인증서버로 요청을 보내고 그 후에 **백엔드의 주소**로 리다이렉트 됩니다.")
    public void kakaoOAuthCallback(HttpServletResponse response) throws IOException {

        String redirectAddress = oAuthService.requestRedirect();
        response.sendRedirect(redirectAddress);
    }


    @Operation(summary = "카카오 인증서버 토큰 검증 API",
    description = "리다이렉트에서 AccessCode를 가지고 서버로 돌아오기 위한 엔드포인트입니다 <br><br><br>" +
            "해당 코드를 이용해서 사용자 정보를 파싱하고 **액세스 토큰과 리프레시 토큰을 모두 쿠키에 담아** 반환합니다 <br><br>" +
            "클라이언트 단에서 다시 한 번 서버로 요청을 주시면 쿠키에 담겨있는 액세스토큰을 헤더로 옮겨 드립니다")
    @GetMapping("/oauth/kakao/callback")
    public void kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse response) {
        oAuthService.kakaoLogin(accessCode,response);
    }
}
