package or.sopt.houme.global.discord;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 5xx 에러 발생 시 디스코드로 traceId 와 함께 알림을 보낸다.
 *
 * <p>설계 원칙:
 * <ul>
 *   <li>발송은 전용 단일 스레드에서 비동기 — 에러 응답 지연 금지</li>
 *   <li>디스코드 장애/실패는 로그만 남기고 무시 — 알림 장애가 API 장애로 전이 금지</li>
 *   <li>동일 시그니처(예외클래스 + URI)는 {@value #COOLDOWN_MINUTES}분 쿨다운 — 장애 시 도배 방지</li>
 * </ul>
 * Sentry(상세 스택 분석)와 역할을 나눈다 — 디스코드는 팀 즉시 인지용 요약.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorAlertNotifier {

    private static final long COOLDOWN_MINUTES = 5;
    private static final int MAX_MESSAGE_LENGTH = 300;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DiscordWebhookService discordWebhookService;

    @Value("${server.env:unknown}")
    private String serverEnv;

    private final Map<String, Instant> lastSentBySignature = new ConcurrentHashMap<>();

    private final ExecutorService alertExecutor = Executors.newSingleThreadExecutor(daemonThreadFactory());

    /**
     * 5xx 에러 알림 발송. 호출 스레드에서 요청 컨텍스트/MDC 를 캡처한 뒤 비동기로 전송한다.
     */
    public void notifyServerError(Throwable exception) {
        try {
            String signature = exception.getClass().getSimpleName() + ":" + currentRequestPath();
            if (isInCooldown(signature)) {
                return;
            }

            // 비동기 스레드에서는 MDC/요청 컨텍스트가 없으므로 지금 캡처한다
            String content = buildContent(exception);
            alertExecutor.execute(() -> discordWebhookService.sendMessage(content));
        } catch (Exception e) {
            log.warn("디스코드 에러 알림 구성 실패 (무시하고 진행)", e);
        }
    }

    private boolean isInCooldown(String signature) {
        Instant now = Instant.now();
        Instant last = lastSentBySignature.get(signature);
        if (last != null && Duration.between(last, now).toMinutes() < COOLDOWN_MINUTES) {
            return true;
        }
        lastSentBySignature.put(signature, now);

        // 시그니처 맵 무한 성장 방지
        if (lastSentBySignature.size() > 1_000) {
            lastSentBySignature.entrySet().removeIf(
                    entry -> Duration.between(entry.getValue(), now).toMinutes() >= COOLDOWN_MINUTES);
        }
        return false;
    }

    private String buildContent(Throwable exception) {
        String traceId = valueOrDash(MDC.get("traceId"));
        String userId = valueOrDash(MDC.get("userId"));
        String requestLine = currentRequestLine();
        String message = exception.getMessage() == null ? "-" : exception.getMessage();
        if (message.length() > MAX_MESSAGE_LENGTH) {
            message = message.substring(0, MAX_MESSAGE_LENGTH) + "…";
        }
        String occurredAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(TIME_FORMAT);

        return """
                🚨 **[%s] %s**
                > **traceId**: `%s`
                > **request**: `%s`
                > **userId**: %s
                > **message**: %s
                > **time**: %s""".formatted(
                serverEnv,
                exception.getClass().getSimpleName(),
                traceId,
                requestLine,
                userId,
                message,
                occurredAt
        );
    }

    private String currentRequestLine() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return "(non-http)";
        }
        return request.getMethod() + " " + request.getRequestURI();
    }

    private String currentRequestPath() {
        HttpServletRequest request = currentRequest();
        return request == null ? "(non-http)" : request.getRequestURI();
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private ThreadFactory daemonThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "discord-error-alert");
            thread.setDaemon(true);
            return thread;
        };
    }
}
