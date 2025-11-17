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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * 동일한 IP에서 1분 내 요청이 20회를 초과하면 차단하는 필터입니다.
 * - 매 요청마다 Redis 카운터를 1 증가시키고, 첫 증가 시 60초 TTL을 설정합니다.
 * - 1분 동안 누적 요청 수가 21번째가 되면 429로 응답하고 체인을 중단합니다.
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
    private static final long LIMIT = 20L; // 1분당 허용 횟수

    private final StringRedisTemplate redisTemplate;
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
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                // 첫 요청: 키에 TTL(1분) 설정
                redisTemplate.expire(key, WINDOW);
            }

            if (count != null && count > LIMIT) {
                // 1분 내 요청 횟수 초과 → 차단 (21번째부터 차단)
                writeTooManyRequests(response);
                return;
            }
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
