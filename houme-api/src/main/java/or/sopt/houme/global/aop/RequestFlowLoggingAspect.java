package or.sopt.houme.global.aop;

import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.logging.TraceIdFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 서비스/파사드 계층의 요청 흐름 추적 AOP.
 *
 * <p>포인트컷은 {@code @Service} 전체 + facade 패키지의 컴포넌트다.
 * (GenerateImageFacade 등 파사드는 @Component 라 @Service 포인트컷만으로는 누락된다)
 *
 * <p>로그 정책:
 * <ul>
 *   <li>진입/탈출 — DEBUG (운영 기본 레벨 INFO 에서는 미출력, 필요 시 로거 레벨로 켠다)</li>
 *   <li>실행시간 {@value #SLOW_CALL_THRESHOLD_MS}ms 초과 — WARN (슬로우 콜 탐지)</li>
 *   <li>예외 통과 — ERROR (traceId 와 조합해 죽은 지점 특정)</li>
 * </ul>
 * 인자 값은 대용량 DTO/민감정보 유출 방지를 위해 로깅하지 않는다.
 * 클래스 내부 자기호출(self-invocation)은 프록시 특성상 추적되지 않는다.
 */
@Slf4j
@Aspect
@Component
public class RequestFlowLoggingAspect {

    private static final long SLOW_CALL_THRESHOLD_MS = 1_000L;

    @Pointcut("@within(org.springframework.stereotype.Service)"
            + " || execution(* or.sopt.houme.domain..facade..*.*(..))")
    public void serviceAndFacadeLayer() {
    }

    @Around("serviceAndFacadeLayer()")
    public Object traceRequestFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();

        log.debug("--> {}", signature);
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > SLOW_CALL_THRESHOLD_MS) {
                log.warn("<-- {} 완료 ({}ms) — 슬로우 콜", signature, elapsed);
            } else {
                log.debug("<-- {} 완료 ({}ms)", signature, elapsed);
            }
            return result;
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("<-x {} 예외 ({}ms): {}", signature, elapsed, e.toString());
            throw e;
        }
    }

    /**
     * 스케줄러 작업은 HTTP 요청 밖에서 실행되어 traceId 가 없으므로 job 단위로 생성한다.
     */
    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object traceScheduledJob(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean created = false;
        if (MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY) == null) {
            MDC.put(TraceIdFilter.TRACE_ID_MDC_KEY, "job-" + UUID.randomUUID().toString().substring(0, 8));
            created = true;
        }
        try {
            return joinPoint.proceed();
        } finally {
            if (created) {
                MDC.clear();
            }
        }
    }
}
