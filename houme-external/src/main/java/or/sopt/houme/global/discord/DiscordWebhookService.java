package or.sopt.houme.global.discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordWebhookService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final ObjectMapper objectMapper;

    @Value("${discord.webhook-url:}")
    private String webhookUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .build();

    public void sendMessage(String content) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("Discord webhook URL is not configured. Skip sending.");
            return;
        }
        if (content == null || content.isBlank()) {
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(new DiscordWebhookPayload(content));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Discord webhook failed. status={}, body={}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Discord webhook failed.", e);
        }
    }

    private record DiscordWebhookPayload(String content) {
    }
}
