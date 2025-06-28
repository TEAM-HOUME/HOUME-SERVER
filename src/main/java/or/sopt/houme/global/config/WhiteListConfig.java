package or.sopt.houme.global.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WhiteListConfig {

    /**
     * 스웨거 API 문서 및 UI 엔드포인트에 대한 화이트리스트 URL 패턴 목록을 반환합니다.
     *
     * @return 스웨거 관련 엔드포인트의 URL 패턴 리스트
     */
    public static final List<String> swaggerWhitelist() {
        return List.of(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        );
    }

    /**
     * OAuth 인증 및 토큰 관련 엔드포인트의 화이트리스트 URL 목록을 반환합니다.
     *
     * @return 인증 없이 접근이 허용되는 OAuth 및 토큰 관련 URL 경로의 불변 리스트
     */
    public static final List<String> oauthWhitelist() {
        return List.of(
                "/oauth/kakao/",
                "/oauth/kakao/callback",
                "/access",
                "/reissue"
        );
    }
}
