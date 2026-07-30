package or.sopt.houme.global.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.legacy.LegacyApiAlertCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Slf4j
@Component
@RequiredArgsConstructor
public class LegacyApiAlertNotifier {

    private static final int MAX_FIELD_LENGTH = 300;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DiscordWebhookService discordWebhookService;
    private final ExecutorService alertExecutor = Executors.newSingleThreadExecutor(daemonThreadFactory());

    @Value("${server.env:unknown}")
    private String serverEnv;

    @Value("${discord.legacy-api-webhook-url:}")
    private String legacyApiWebhookUrl;

    public void notifyLegacyApiCall(LegacyApiAlertCommand command) {
        try {
            String content = buildContent(command);
            alertExecutor.execute(() -> discordWebhookService.sendMessageTo(legacyApiWebhookUrl, content));
        } catch (Exception e) {
            log.warn("레거시 API 디스코드 알림 구성 실패 (무시하고 진행)", e);
        }
    }

    private String buildContent(LegacyApiAlertCommand command) {
        String occurredAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(TIME_FORMAT);

        return """
                ⚠️ **[%s] Legacy API Called**
                > **notice**: 삭제 후보 API의 호출이 발생했습니다. 호출 주체 확인이 필요합니다.
                > **method**: `%s`
                > **documentedPath**: `%s`
                > **requestUri**: `%s`
                > **queryString**: `%s`
                > **userId**: %s
                > **traceId**: `%s`
                > **reason**: %s
                > **referer**: `%s`
                > **userAgent**: `%s`
                > **time**: %s""".formatted(
                serverEnv,
                command.method(),
                command.documentedPath(),
                command.requestUri(),
                trim(command.queryString()),
                command.userId(),
                command.traceId(),
                trim(command.reason()),
                trim(command.referer()),
                trim(command.userAgent()),
                occurredAt
        );
    }

    private String trim(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.length() > MAX_FIELD_LENGTH ? value.substring(0, MAX_FIELD_LENGTH) + "..." : value;
    }

    private ThreadFactory daemonThreadFactory() {
        return runnable -> {
            Thread thread = new Thread(runnable, "discord-legacy-api-alert");
            thread.setDaemon(true);
            return thread;
        };
    }
}
