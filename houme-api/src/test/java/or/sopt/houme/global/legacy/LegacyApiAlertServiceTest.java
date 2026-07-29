package or.sopt.houme.global.legacy;

import or.sopt.houme.global.discord.LegacyApiAlertNotifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LegacyApiAlertServiceTest {

    private final LegacyApiAlertNotifier legacyApiAlertNotifier = mock(LegacyApiAlertNotifier.class);
    private final LegacyApiAlertService legacyApiAlertService = new LegacyApiAlertService(legacyApiAlertNotifier);

    @Test
    @DisplayName("레거시 API 알림이 비활성화되어 있으면 디스코드 알림을 보내지 않는다")
    void notifyIfNeeded_disabled() {
        ReflectionTestUtils.setField(legacyApiAlertService, "enabled", false);

        legacyApiAlertService.notifyIfNeeded(command("GET", "/api/v1/carousels"));

        verify(legacyApiAlertNotifier, never()).notifyLegacyApiCall(command("GET", "/api/v1/carousels"));
    }

    @Test
    @DisplayName("같은 레거시 API 호출은 쿨다운 시간 동안 한 번만 알림을 보낸다")
    void notifyIfNeeded_cooldown() {
        ReflectionTestUtils.setField(legacyApiAlertService, "enabled", true);
        ReflectionTestUtils.setField(legacyApiAlertService, "cooldownMinutes", 5L);

        LegacyApiAlertCommand command = command("GET", "/api/v1/carousels");

        legacyApiAlertService.notifyIfNeeded(command);
        legacyApiAlertService.notifyIfNeeded(command);

        verify(legacyApiAlertNotifier, times(1)).notifyLegacyApiCall(command);
    }

    @Test
    @DisplayName("HTTP method 또는 path가 다르면 별도 레거시 API로 보고 각각 알림을 보낸다")
    void notifyIfNeeded_differentSignature() {
        ReflectionTestUtils.setField(legacyApiAlertService, "enabled", true);
        ReflectionTestUtils.setField(legacyApiAlertService, "cooldownMinutes", 5L);

        LegacyApiAlertCommand getCommand = command("GET", "/api/v1/sign-up");
        LegacyApiAlertCommand postCommand = command("POST", "/api/v1/sign-up");

        legacyApiAlertService.notifyIfNeeded(getCommand);
        legacyApiAlertService.notifyIfNeeded(postCommand);

        verify(legacyApiAlertNotifier, times(1)).notifyLegacyApiCall(getCommand);
        verify(legacyApiAlertNotifier, times(1)).notifyLegacyApiCall(postCommand);
    }

    private LegacyApiAlertCommand command(String method, String documentedPath) {
        return new LegacyApiAlertCommand(
                method,
                documentedPath,
                documentedPath,
                "-",
                "test reason",
                "trace-id",
                "1",
                "JUnit",
                "-"
        );
    }
}
