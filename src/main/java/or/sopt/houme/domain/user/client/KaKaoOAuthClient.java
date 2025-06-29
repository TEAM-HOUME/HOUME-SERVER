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
     * 클라이언트 단에서 로그인을 요청했을때, 인가 코드를 가져옵니다
     * 이를 통해 회원의 정보에 접근 할 수 있는 accessToken을 요청 할 수 있습니다
     *
     * 하지만 해당 요청 메서드는 별도로 사용되지 않고, 클라이언트를 직접 카카오 로그인 서비스로
     * 리다이렉트 시키는 것으로 대체됩니다
     * */
    @GetMapping("/oauth/authorize")
    KaKaoCallbackResponse getCode(@RequestParam("client_id") String client_id,
                                  @RequestParam("redirect_uri") String redirect_uri,
                                  @RequestParam("response_type") String response_type);


    /**
     * 클라이언트의 액세스 코드를 가지고 액세스 토큰을 받아오는 메서드입니다
     *
     * 이전에 설정해놓은 리다이렉트 주소를 통해 요청이 액세스 코드를 가져오면
     * 이를 가지고 액세스 토큰을 반환받습니다
     *
     * @param grant_type: 해당 파라미터는 authorization_code 로 고정됩니다
     * */
    @PostMapping("/oauth/token")
    KaKaoOAuthTokenDTO getToken(@RequestParam("grant_type") String grant_type,
                                @RequestParam("client_id") String client_id,
                                @RequestParam("redirect_uri") String redirect_uri,
                                @RequestParam("code") String code);

}
