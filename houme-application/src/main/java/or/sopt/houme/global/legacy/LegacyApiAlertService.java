package or.sopt.houme.global.legacy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LegacyApiAlertService {

    private final or.sopt.houme.global.discord.LegacyApiAlertNotifier legacyApiAlertNotifier;

    @Value("${legacy-api-alert.enabled:false}")
    private boolean enabled;

    @Value("${legacy-api-alert.cooldown-minutes:5}")
    private long cooldownMinutes;

    private final Map<String, Instant> lastSentBySignature = new ConcurrentHashMap<>();

    public void notifyIfNeeded(LegacyApiAlertCommand command) {
        if (!enabled || command == null) {
            return;
        }

        String signature = command.method() + " " + command.documentedPath();
        if (isInCooldown(signature)) {
            return;
        }

        legacyApiAlertNotifier.notifyLegacyApiCall(command);
    }

    private boolean isInCooldown(String signature) {
        Instant now = Instant.now();
        Instant winner = lastSentBySignature.compute(signature, (key, last) ->
                last != null && Duration.between(last, now).toMinutes() < cooldownMinutes ? last : now);

        if (winner != now) {
            return true;
        }

        if (lastSentBySignature.size() > 1_000) {
            lastSentBySignature.entrySet().removeIf(
                    entry -> Duration.between(entry.getValue(), now).toMinutes() >= cooldownMinutes);
        }
        return false;
    }
}
