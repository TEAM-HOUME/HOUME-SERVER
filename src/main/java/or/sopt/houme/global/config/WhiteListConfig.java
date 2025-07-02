package or.sopt.houme.global.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WhiteListConfig {

    // 스웨거 관련 인가 설정
    public static final List<String> swaggerWhitelist() {
        return List.of(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        );
    }

    // oauth 관련 인가 설정
    public static final List<String> oauthWhitelist() {
        return List.of(
                "/oauth/kakao/",
                "/oauth/kakao/callback",
                "/access",
                "/reissue"
        );
    }

    // oauth 관련 인가 설정
    public static final List<String> serverWhitelist() {
        return List.of(
                "/actuator/health"
        );
    }
}
