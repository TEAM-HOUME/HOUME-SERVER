package or.sopt.houme.global.legacy;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.global.logging.TraceIdFilter;
import or.sopt.houme.legacyapi.application.LegacyApiCallHistoryService;
import or.sopt.houme.legacyapi.domain.LegacyApiCall;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;
import org.slf4j.MDC;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/** Swagger 삭제 후보 표기가 있는 API의 호출 이력만 비동기로 기록한다. */
@Slf4j
@Component
public class LegacyApiCallHistoryInterceptor implements HandlerInterceptor {

    private static final String CANDIDATE_ATTRIBUTE = "houme.legacyApiCallHistory.candidate";
    private final LegacyApiCallHistoryService legacyApiCallHistoryService;
    private final Executor legacyApiCallHistoryExecutor;
    private final Counter droppedCounter;
    private final Counter failedCounter;

    public LegacyApiCallHistoryInterceptor(
            LegacyApiCallHistoryService legacyApiCallHistoryService,
            @Qualifier("legacyApiCallHistoryExecutor") Executor legacyApiCallHistoryExecutor,
            MeterRegistry meterRegistry
    ) {
        this.legacyApiCallHistoryService = legacyApiCallHistoryService;
        this.legacyApiCallHistoryExecutor = legacyApiCallHistoryExecutor;
        this.droppedCounter = meterRegistry.counter("houme.legacy-api-call-history.dropped.total");
        this.failedCounter = meterRegistry.counter("houme.legacy-api-call-history.write-failed.total");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod && isDeprecatedCandidate(handlerMethod)) {
            request.setAttribute(CANDIDATE_ATTRIBUTE, Boolean.TRUE);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object candidate = request.getAttribute(CANDIDATE_ATTRIBUTE);
        if (candidate != Boolean.TRUE) {
            return;
        }

        LegacyApiCall legacyApiCall = new LegacyApiCall(
                request.getMethod(),
                request.getRequestURI(),
                currentUserId(),
                MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)
        );

        try {
            legacyApiCallHistoryExecutor.execute(() -> recordSafely(legacyApiCall));
        } catch (RejectedExecutionException e) {
            droppedCounter.increment();
            log.warn("레거시 API 호출 이력 저장 작업이 포화로 드롭되었습니다. method={}, requestUri={}",
                    legacyApiCall.method(), legacyApiCall.requestUri());
        }
    }

    private boolean isDeprecatedCandidate(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(LegacyApi.class);
    }

    private void recordSafely(LegacyApiCall legacyApiCall) {
        try {
            legacyApiCallHistoryService.record(legacyApiCall);
        } catch (RuntimeException e) {
            failedCounter.increment();
            log.error("레거시 API 호출 이력 저장에 실패했습니다. method={}, requestUri={}",
                    legacyApiCall.method(), legacyApiCall.requestUri(), e);
        }
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return null;
        }
        return userDetails.getUser() == null ? null : userDetails.getUser().getId();
    }
}
