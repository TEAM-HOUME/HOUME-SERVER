package or.sopt.houme.global.config;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.global.legacy.LegacyApiCallHistoryInterceptor;
import or.sopt.houme.global.logging.LoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final LegacyApiCallHistoryInterceptor legacyApiCallHistoryInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/favicon.ico"
                );
        registry.addInterceptor(legacyApiCallHistoryInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/favicon.ico"
                );
    }
}
