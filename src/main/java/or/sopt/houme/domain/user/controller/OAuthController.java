package or.sopt.houme.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "소셜로그인 관련 API")
public class OAuthController {

    private final OAuthService oAuthService;

    /**
     * 카카오 소셜 로그인 인증을 시작하고, 사용자를 카카오 인증 서버로 리다이렉트합니다.
     *
     * 카카오 인증 서버로 리다이렉트된 후, 인증이 완료되면 인가 코드가 포함된 상태로 클라이언트가 다시 리다이렉트됩니다.
     *
     * @param response 클라이언트를 리다이렉트하기 위한 HTTP 응답 객체
     * @throws IOException 리다이렉트 처리 중 입출력 오류가 발생한 경우
     */
    @GetMapping("/oauth/kakao")
    @Operation(summary = "카카오 소셜로그인 API",
            description = "카카오로 로그인 요청을 전송합니다. <br><br>" +
                    "카카오 인증서버로 요청을 보내고 그 후에 **localhost:3000**로 리다이렉트 됩니다. <br><br>" +
                    "리다이렉트 주소에 포함되어 있는 인가코드를 밑의 API의 파라미터에 넣어주세요")
    public void kakaoOAuthCallback(HttpServletResponse response) throws IOException {

        String redirectAddress = oAuthService.requestRedirect();
        response.sendRedirect(redirectAddress);
    }


    /**
     * 카카오 OAuth 인증 후 콜백 요청을 처리하여 사용자 정보를 파싱하고 토큰을 발급합니다.
     *
     * @param accessCode 카카오 인증 서버에서 전달된 인가 코드
     */
    @Operation(summary = "카카오 인증서버 토큰 검증 API",
    description = "리다이렉트에서 AccessCode를 가지고 서버로 돌아오기 위한 엔드포인트입니다 <br><br><br>" +
            "해당 코드를 이용해서 사용자 정보를 파싱하고 **액세스 토큰는 헤더에, 리프레시 토큰은 쿠키에 담아** 반환합니다")
    @GetMapping("/oauth/kakao/callback")
    public void kakaoLogin(@RequestParam("code") String accessCode, HttpServletResponse response) {
        oAuthService.kakaoLogin(accessCode,response);
    }
}
