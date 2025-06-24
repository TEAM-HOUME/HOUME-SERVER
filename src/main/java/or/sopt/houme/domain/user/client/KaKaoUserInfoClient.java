package or.sopt.houme.domain.user.client;


import or.sopt.houme.domain.user.controller.dto.KaKaoUserInfoResponse;
import or.sopt.houme.global.config.KaKaoOAuthFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "kakaoUserInfoClient",
        url = "https://kapi.kakao.com",
        configuration = KaKaoOAuthFeignConfig.class
)
public interface KaKaoUserInfoClient {

    /**
     * 사용자에 대한 액세스 토큰으로 사용자 정보를 가져옵니다
     * 이때 헤더는 당연하겠지만 반드시 "Bearer " 가 추가된 채로 파라미터로 들어와야합니다
     * */
    @PostMapping("/v2/user/me")
    KaKaoUserInfoResponse getUserInfo(@RequestHeader("Authorization") String accessToken);
}
