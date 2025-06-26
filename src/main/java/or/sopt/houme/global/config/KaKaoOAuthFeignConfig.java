package or.sopt.houme.global.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KaKaoOAuthFeignConfig {

    @Bean
    public RequestInterceptor kakaoRequestInterceptor() {
        return template -> {
            template.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        };
    }
}
