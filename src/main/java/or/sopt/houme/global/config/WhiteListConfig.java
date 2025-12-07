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
                "/oauth/kakao",
                "/oauth/kakao/callback",
                "/access",
                "/reissue"
        );
    }

    // oauth 관련 인가 설정
    public static final List<String> serverWhitelist() {
        return List.of(
                "/actuator/health",
                "/api/v1/env",
                "/image/generate"
        );
    }

    // 집 이미지 생성하는 플로우 설정들
    public static final List<String> makeHouseWhitelist() {
        return List.of(
                "/api/v1/housing-options",
                "/api/v1/housing-selections",
                "/api/v1/addresses",
                "/api/v1/moodboard-images"
        );
    }

    // 회원 관련 인가 설정
    public static final List<String> userWhiteList() {
        return List.of(
                "/api/v1/check-has-generated-image"
        );
    }

    // 모니터링 관련 인가 설정
    public static final List<String> monitoringWhiteList() {
        return List.of(
                "/actuator/prometheus",
                "/actuator/**"
        );
    }

    public static final List<String> adminWhiteList() {
        return List.of(
                "/api/v1/admin/register",
                "/admin/login"
        );
    }
}
