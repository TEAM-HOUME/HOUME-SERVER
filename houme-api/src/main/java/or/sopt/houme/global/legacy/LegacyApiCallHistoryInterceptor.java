package or.sopt.houme.global.legacy;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.method.HandlerMethod;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/** {@link LegacyApi}가 붙은 삭제 후보 API의 호출 이력만 비동기로 기록한다. */
@Slf4j
@Component
public class LegacyApiCallHistoryInterceptor implements HandlerInterceptor {

    private static final String CANDIDATE_ATTRIBUTE = "houme.legacyApiCallHistory.candidate";
    private static final Duration RECORD_INTERVAL = Duration.ofHours(1);
    private final LegacyApiCallHistoryService legacyApiCallHistoryService;
    private final Executor legacyApiCallHistoryExecutor;
    private final Map<String, Instant> lastRecordAttemptAt = new ConcurrentHashMap<>();

    public LegacyApiCallHistoryInterceptor(
            LegacyApiCallHistoryService legacyApiCallHistoryService,
            @Qualifier("legacyApiCallHistoryExecutor") Executor legacyApiCallHistoryExecutor
    ) {
        this.legacyApiCallHistoryService = legacyApiCallHistoryService;
        this.legacyApiCallHistoryExecutor = legacyApiCallHistoryExecutor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod && isDeprecatedCandidate(handlerMethod)) {
            Object apiPath = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (apiPath != null) {
                request.setAttribute(CANDIDATE_ATTRIBUTE, apiPath.toString());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object candidate = request.getAttribute(CANDIDATE_ATTRIBUTE);
        if (!(candidate instanceof String apiPath)) {
            return;
        }

        LegacyApiCall legacyApiCall = new LegacyApiCall(
                request.getMethod(),
                request.getRequestURI(),
                apiPath,
                currentUserId(),
                MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)
        );
        String callKey = legacyApiCall.method() + " " + legacyApiCall.apiPath();
        Instant attemptedAt = Instant.now();
        if (!reserveRecord(callKey, attemptedAt)) {
            return;
        }

        try {
            legacyApiCallHistoryExecutor.execute(() -> recordSafely(callKey, attemptedAt, legacyApiCall));
        } catch (RejectedExecutionException e) {
            lastRecordAttemptAt.remove(callKey, attemptedAt);
            log.warn("레거시 API 호출 이력 저장 작업이 포화로 드롭되었습니다. method={}, apiPath={}",
                    legacyApiCall.method(), legacyApiCall.apiPath());
        }
    }

    private boolean isDeprecatedCandidate(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(LegacyApi.class);
    }

    private boolean reserveRecord(String callKey, Instant attemptedAt) {
        AtomicBoolean reserved = new AtomicBoolean(false);
        lastRecordAttemptAt.compute(callKey, (key, lastAttemptAt) -> {
            if (lastAttemptAt == null || !lastAttemptAt.plus(RECORD_INTERVAL).isAfter(attemptedAt)) {
                reserved.set(true);
                return attemptedAt;
            }
            return lastAttemptAt;
        });
        return reserved.get();
    }

    private void recordSafely(String callKey, Instant attemptedAt, LegacyApiCall legacyApiCall) {
        try {
            legacyApiCallHistoryService.record(legacyApiCall);
        } catch (RuntimeException e) {
            lastRecordAttemptAt.remove(callKey, attemptedAt);
            log.error("레거시 API 호출 이력 저장에 실패했습니다. method={}, apiPath={}",
                    legacyApiCall.method(), legacyApiCall.apiPath(), e);
        }
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    void evictExpiredRecordReservations() {
        Instant now = Instant.now();
        lastRecordAttemptAt.entrySet().removeIf(entry -> entry.getValue().plus(RECORD_INTERVAL).isBefore(now));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return null;
        }
        return userDetails.getUser() == null ? null : userDetails.getUser().getId();
    }
}
