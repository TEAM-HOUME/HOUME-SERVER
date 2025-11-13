package or.sopt.houme.global.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    @Bean
    public Request.Options feignRequestOptions() {

        // 1분(60초) 문제를 해결하기 위해 130초(130,000ms)로 설정합니다.
        // (Resilience4j의 TimeLimiter(120초)보다 길게 설정)
        long connectTimeout = 5_000; // 5초
        long readTimeout = 130_000;  // 130초

        return new Request.Options(
                connectTimeout, TimeUnit.MILLISECONDS,
                readTimeout, TimeUnit.MILLISECONDS,
                true // followRedirects (리다이렉션 허용 여부)
        );
    }
}
