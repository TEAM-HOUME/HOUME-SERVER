package or.sopt.houme.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP 액세스 로그 인터셉터.
 *
 * <p>요청 1건당 완료 시점에 한 줄(method, uri, status, latency)을 남긴다.
 * traceId/userId 는 MDC 패턴으로 자동 포함되므로 여기서 다시 찍지 않는다.
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "houme.request.startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object startTime = request.getAttribute(START_TIME_ATTRIBUTE);
        long latencyMs = startTime instanceof Long start ? System.currentTimeMillis() - start : -1;

        String query = request.getQueryString();
        String uri = query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;

        if (ex != null || response.getStatus() >= 500) {
            log.error("HTTP {} {} -> {} ({}ms)", request.getMethod(), uri, response.getStatus(), latencyMs);
            return;
        }
        log.info("HTTP {} {} -> {} ({}ms)", request.getMethod(), uri, response.getStatus(), latencyMs);
    }
}
