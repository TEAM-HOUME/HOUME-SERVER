package or.sopt.houme.global.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KaKaoOAuthFeignConfig {

    /**
     * 모든 Feign 클라이언트 요청에 "Content-Type: application/x-www-form-urlencoded;charset=utf-8" 헤더를 추가하는 인터셉터를 생성합니다.
     *
     * @return Feign 요청에 지정된 Content-Type 헤더를 자동으로 설정하는 RequestInterceptor 인스턴스
     */
    @Bean
    public RequestInterceptor kakaoRequestInterceptor() {
        return template -> {
            template.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        };
    }
}
