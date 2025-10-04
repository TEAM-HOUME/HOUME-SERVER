package or.sopt.houme.global.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class NaverFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // FULL 로 바꾸면 요청/응답 전체 로깅 가능
    }

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                3000, TimeUnit.MILLISECONDS,
                5000, TimeUnit.MILLISECONDS,
                true
        );
    }
}
