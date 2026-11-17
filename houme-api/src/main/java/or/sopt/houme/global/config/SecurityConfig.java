package or.sopt.houme.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.service.OAuthService;
import or.sopt.houme.global.jwt.JWTFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] ADMIN_PATHS = {
            "/api/v1/admin/**",
            "/api/admin/**",
            "/admin/**"
    };

    private static final String[] FOR_PLAN_PATHS = {
            "/api/v1/generated-images/*/curations/products/*/for-plan",
            "/api/v1/generated-images/*/curations/products/*/for-plan/detail"
    };

    private final JWTFilter jwtFilter;
    private final OAuthService oAuthService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager Bean 등록, UsernamePasswordAuthenticationFilter에서 필요하기 때문에 생성자로 주입한다
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        /**
                         * CORS 정책 대상 URL을 리스트로 관리
                         * 다양한 메서드에 대해 CORS 정책을 허용합니다
                         * */
                        configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:5173",
                                "http://localhost:5174",
                                "http://localhost:5175", // 백오피스 클라이언트 로컬 개발
                                "https://dev.houme.kr",
                                "https://www.houme.kr",
                                "https://preview.houme.kr",
                                "https://prod.houme.kr"
                        ));
                        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        // exposedHeaders에 중복 설정 제거하고, 두 개의 헤더를 노출
                        configuration.setExposedHeaders(Arrays.asList(
                                "Set-Cookie",
                                "access",
                                "Authorization",
                                "access-token"
                        ));


                        return configuration;
                    }
                }));




        http.csrf(AbstractHttpConfigurer::disable);

        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);


        // 인가 경로 설정
        http.authorizeHttpRequests((auth)->auth
                .requestMatchers(HttpMethod.POST, "/api/v1/sign-up").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v2/sign-up").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v2/nickname/rotate").permitAll()
                .requestMatchers(WhiteListConfig.swaggerWhitelist().toArray(new String[0])).permitAll()
                .requestMatchers(WhiteListConfig.oauthWhitelist().toArray(new String[0])).permitAll()
                .requestMatchers(WhiteListConfig.serverWhitelist().toArray(new String[0])).permitAll()
                .requestMatchers(WhiteListConfig.makeHouseWhitelist().toArray(new String[0])).permitAll()
                .requestMatchers(WhiteListConfig.userWhiteList().toArray(new String[0])).permitAll()
                .requestMatchers(WhiteListConfig.exploreWhiteList().toArray(new String[0])).permitAll()
                .requestMatchers(WhiteListConfig.monitoringWhiteList().toArray(new String[0])).permitAll()
                .requestMatchers(WhiteListConfig.adminWhiteList().toArray(new String[0])).permitAll()
                .requestMatchers(WhiteListConfig.curationWhiteList().toArray(new String[0])).permitAll()
                .requestMatchers(HttpMethod.GET, WhiteListConfig.compareWhiteList().toArray(new String[0])).permitAll()
                // 어드민 API·SSR 대시보드는 어드민 토큰(ROLE_ADMIN)만 허용
                .requestMatchers(ADMIN_PATHS).hasAuthority("ROLE_ADMIN")
                // 기획 의사결정용 for-plan 조회는 어드민 대시보드도 호출하므로 회원·어드민 토큰 모두 허용
                .requestMatchers(HttpMethod.GET, FOR_PLAN_PATHS).authenticated()
                .requestMatchers("/error").authenticated()
                // 나머지 회원 API는 회원 토큰만 허용 — 어드민 토큰으로는 회원 API를 호출할 수 없다
                .anyRequest().access((authentication, context) ->
                        new AuthorizationDecision(authentication.get().getPrincipal() instanceof CustomUserDetails)));


        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.logout(AbstractHttpConfigurer::disable);

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
