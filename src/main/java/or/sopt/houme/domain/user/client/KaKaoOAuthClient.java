package or.sopt.houme.domain.user.client;



import or.sopt.houme.domain.user.controller.dto.KaKaoCallbackResponse;
import or.sopt.houme.domain.user.controller.dto.KaKaoOAuthTokenDTO;
import or.sopt.houme.global.config.KaKaoOAuthFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "kakaoOAuthClient",
        url = "https://kauth.kakao.com",
        configuration = KaKaoOAuthFeignConfig.class
)
public interface KaKaoOAuthClient {


    /**
                                   * 카카오 OAuth 인가 코드를 요청합니다.
                                   *
                                   * 클라이언트가 카카오 로그인을 시작할 때 인가 코드를 발급받기 위해 호출하는 메서드입니다.
                                   * 실제 서비스에서는 클라이언트를 카카오 로그인 페이지로 직접 리다이렉트하여 이 과정을 대체합니다.
                                   *
                                   * @param client_id 카카오 REST API 키
                                   * @param redirect_uri 인가 코드 발급 후 리다이렉트될 URI
                                   * @param response_type 응답 타입(일반적으로 "code" 사용)
                                   * @return 카카오 인가 코드 응답 객체
                                   */
    @GetMapping("/oauth/authorize")
    KaKaoCallbackResponse getCode(@RequestParam("client_id") String client_id,
                                  @RequestParam("redirect_uri") String redirect_uri,
                                  @RequestParam("response_type") String response_type);


    /**
                                 * 카카오 OAuth 인증 코드로 액세스 토큰을 요청합니다.
                                 *
                                 * 사용자가 인증을 완료한 후 발급받은 인증 코드(code)를 이용해 카카오로부터 액세스 토큰을 발급받습니다.
                                 *
                                 * @param grant_type 토큰 발급 방식(항상 "authorization_code"로 지정)
                                 * @param client_id 카카오 OAuth 애플리케이션의 REST API 키
                                 * @param redirect_uri 인증 코드 발급 시 사용한 리다이렉트 URI
                                 * @param code 카카오 인증 후 발급받은 인증 코드
                                 * @return 카카오에서 발급한 액세스 토큰 정보
                                 */
    @PostMapping("/oauth/token")
    KaKaoOAuthTokenDTO getToken(@RequestParam("grant_type") String grant_type,
                                @RequestParam("client_id") String client_id,
                                @RequestParam("redirect_uri") String redirect_uri,
                                @RequestParam("code") String code);

}
