package or.sopt.houme.domain.generateImage.infrastructure.gemini.service;

import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.ChatGptException;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Profile("load_test")
@Slf4j
public class GeminiImageStubService implements GeminiImageService {

    @Value("${load-test.gemini.min-delay-ms:90000}")
    private long minDelayMs;

    @Value("${load-test.gemini.max-delay-ms:120000}")
    private long maxDelayMs;

    @Value("${load-test.gemini.failure-rate:0.02}")
    private double failureRate;

    @Value("${load-test.gemini.mock-image-url:https://example.com/load-test-gemini.png}")
    private String mockImageUrl;

    @Override
    public ImageUploadResponseDTO createImage(String prompt) {
        long delay = resolveDelay();
        log.info("[load_test] Gemini Stub 호출 - delay={}ms", delay);
        sleep(delay);

        if (isFailure()) {
            log.warn("[load_test] Gemini Stub 실패 시뮬레이션");
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }

        String filename = "load-test-gemini-" + UUID.randomUUID() + ".png";
        ImageUploadResponseDTO responseDTO = ImageUploadResponseDTO.builder()
                .filename(filename)
                .originalFilename(filename)
                .imageLink(mockImageUrl)
                .contentType("image/png")
                .build();
        responseDTO.setPullPrompt(prompt);
        return responseDTO;
    }

    private long resolveDelay() {
        long safeMin = Math.max(0L, minDelayMs);
        long safeMax = Math.max(safeMin, maxDelayMs);
        if (safeMin == safeMax) {
            return safeMin;
        }
        return ThreadLocalRandom.current().nextLong(safeMin, safeMax + 1);
    }

    private boolean isFailure() {
        double safeFailureRate = Math.max(0.0, Math.min(1.0, failureRate));
        return ThreadLocalRandom.current().nextDouble() < safeFailureRate;
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChatGptException(ErrorCode.CHAT_GPT_CALL_EXCEPTION);
        }
    }
}
