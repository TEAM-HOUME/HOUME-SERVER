package or.sopt.houme.global.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    @Bean
    public Request.Options feignRequestOptions() {

        // Gemini 이미지 생성은 2분 이상 걸릴 수 있어 여유 있게 설정합니다.
        long connectTimeout = 5_000; // 5초
        long readTimeout = 300_000;  // 300초

        return new Request.Options(
                connectTimeout, TimeUnit.MILLISECONDS,
                readTimeout, TimeUnit.MILLISECONDS,
                true // followRedirects (리다이렉션 허용 여부)
        );
    }
}
