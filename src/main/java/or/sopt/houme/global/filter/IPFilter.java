package or.sopt.houme.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.ApiResponse;
import or.sopt.houme.global.api.ErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * 동일한 IP에서 1분 내 2번 이상 요청이 들어오면 차단하는 필터입니다.
 * - 첫 요청은 허용하고, Redis에 60초 TTL 키를 저장합니다.
 * - 같은 IP로 TTL이 남아있는 동안 재요청 시 429로 응답하고 체인을 중단합니다.
 *
 * 참고 사항:
 * - CORS Preflight(OPTIONS)는 필터링하지 않습니다.
 * - 테스트 환경(`test` 프로파일)에서는 비활성화하여 테스트에 영향이 없도록 합니다.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class IPFilter extends OncePerRequestFilter {

    private static final String RATE_KEY_PREFIX = "ip:rate:";
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        // Preflight 및 공통 인프라 엔드포인트는 제외
        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;

        String uri = request.getRequestURI();
        if (uri == null) return false;

        // 모니터링/도구용 actuator, swagger 엔드포인트는 제외
        if (uri.startsWith("/actuator")) return true;
        if (uri.startsWith("/v3/api-docs")) return true;
        if (uri.startsWith("/swagger-ui")) return true;
        if ("/swagger-ui.html".equals(uri)) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        String key = RATE_KEY_PREFIX + clientIp;

        try {
            Boolean exists = redisTemplate.hasKey(key);

            if (Boolean.TRUE.equals(exists)) {
                // 같은 IP로 1분 내 재요청 발생 → 차단
                writeTooManyRequests(response);
                return;
            }

            // 첫 요청: 짧은 TTL(1분) 키 저장
            redisTemplate.opsForValue().set(key, 1, WINDOW);
        } catch (Exception e) {
            // Fail-open 전략: Redis 에러 시 서비스 중단 방지 위해 통과
            log.warn("IPFilter encountered an error; allowing request. ip={}, err={}", clientIp, e.toString());
        }

        filterChain.doFilter(request, response);
    }


    /**
     * 예외를 필터단에서 직접 조작하여 반환하는 메서드
     * */
    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        ErrorCode error = ErrorCode.RETRY_GET_IMAGE;
        ApiResponse<Void> body = ApiResponse.fail(error.getCode(), error.getMsg());
        String json = objectMapper.writeValueAsString(body);

        response.setStatus(error.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(json);
        response.getWriter().flush();
    }


    /**
     * 클라이언트의 IP를 파싱하는 메서드
     * */
    private String resolveClientIp(HttpServletRequest request) {

        // 프록시 환경 고려: X-Forwarded-For의 첫 IP → X-Real-IP → remoteAddr 순으로 사용
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) return first;
        }
        String xri = request.getHeader("X-Real-IP");
        return Optional.ofNullable(xri).filter(ip -> !ip.isBlank()).orElse(request.getRemoteAddr());
    }
}
