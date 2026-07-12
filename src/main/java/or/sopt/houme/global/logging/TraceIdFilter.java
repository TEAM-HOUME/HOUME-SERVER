package or.sopt.houme.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 모든 요청에 traceId 를 부여하는 최전방 필터.
 *
 * <p>서블릿 컨테이너 필터 체인의 최우선 순위로 등록되어 스프링 시큐리티 체인(JWTFilter 포함)보다
 * 먼저 실행된다. 따라서 필터 단에서 발생하는 인증 에러 응답/로그에도 traceId 가 포함된다.
 *
 * <p>클라이언트/게이트웨이가 {@value #REQUEST_ID_HEADER} 헤더를 보내면 그대로 수용해
 * 시스템 간 추적을 잇고, 없으면 UUID 앞 8자리를 생성한다.
 * 응답에는 항상 {@value #TRACE_ID_HEADER} 헤더로 되돌려준다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final int TRACE_ID_LENGTH = 8;

    // 외부 입력(X-Request-Id)은 영숫자/._- 만 수용 — JSON/로그 주입 원천 차단
    private static final java.util.regex.Pattern SAFE_TRACE_ID =
            java.util.regex.Pattern.compile("^[A-Za-z0-9._-]{1,36}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId != null && SAFE_TRACE_ID.matcher(requestId.trim()).matches()) {
            return requestId.trim();
        }
        // 헤더가 없거나 허용 문자 밖이면 신뢰하지 않고 새로 발급한다 (JSON/로그 주입 차단)
        return UUID.randomUUID().toString().substring(0, TRACE_ID_LENGTH);
    }
}
