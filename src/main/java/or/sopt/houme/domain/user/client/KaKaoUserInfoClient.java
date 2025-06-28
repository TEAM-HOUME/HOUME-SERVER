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
     * 카카오 액세스 토큰을 사용하여 사용자 정보를 조회합니다.
     *
     * @param accessToken "Bearer " 접두사가 포함된 카카오 액세스 토큰
     * @return 카카오 사용자 정보 응답 객체
     */
    @PostMapping("/v2/user/me")
    KaKaoUserInfoResponse getUserInfo(@RequestHeader("Authorization") String accessToken);
}
