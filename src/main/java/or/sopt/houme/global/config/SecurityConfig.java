package or.sopt.houme.global.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.global.jwt.JWTFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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

    private final JWTFilter jwtFilter;


    /**
     * BCrypt 해싱 알고리즘을 사용하는 비밀번호 인코더 빈을 생성합니다.
     *
     * @return 비밀번호를 안전하게 해싱하기 위한 BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 빈을 생성하여 반환합니다.
     *
     * AuthenticationConfiguration에서 AuthenticationManager를 가져와 반환하며,
     * 인증 관련 필터(예: UsernamePasswordAuthenticationFilter)에서 사용됩니다.
     *
     * @param configuration 인증 매니저 구성을 위한 AuthenticationConfiguration 인스턴스
     * @return AuthenticationManager 인스턴스
     * @throws Exception AuthenticationManager를 가져올 수 없는 경우 예외가 발생할 수 있습니다.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }


    /**
     * Spring Security의 HTTP 보안 필터 체인을 구성하여 CORS, CSRF, 인증 및 인가 정책을 설정합니다.
     *
     * CORS 정책을 커스터마이즈하여 특정 오리진, 메서드, 헤더, 인증 정보를 허용하며, CSRF 보호와 폼 로그인, HTTP Basic 인증을 비활성화합니다.
     * Swagger 및 OAuth 관련 화이트리스트 경로는 인증 없이 접근을 허용하고, 그 외 모든 요청은 인증이 필요합니다.
     * 세션 관리는 무상태(Stateless)로 설정되며, JWT 기반 인증을 위해 커스텀 JWT 필터가 UsernamePasswordAuthenticationFilter 앞에 추가됩니다.
     *
     * @return 구성된 SecurityFilterChain 인스턴스
     * @throws Exception 보안 설정 중 오류가 발생할 경우
     */
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
                                "http://localhost:3000"
                        ));
                        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        // exposedHeaders에 중복 설정 제거하고, 두 개의 헤더를 노출
                        configuration.setExposedHeaders(Arrays.asList("Set-CookieUtil", "access", "Authorization"));

                        return configuration;
                    }
                }));




        http.csrf(AbstractHttpConfigurer::disable);

        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);


        // 인가 경로 설정
        http.authorizeHttpRequests((auth)->auth
                .requestMatchers(WhiteListConfig.swaggerWhitelist().toArray(new String[0])).permitAll()
                .requestMatchers(WhiteListConfig.oauthWhitelist().toArray(new String[0])).permitAll()
                .anyRequest().authenticated());

        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

