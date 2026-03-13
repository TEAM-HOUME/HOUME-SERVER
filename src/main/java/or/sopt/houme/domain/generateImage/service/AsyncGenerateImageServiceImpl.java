package or.sopt.houme.domain.generateImage.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.infrastructure.gemini.service.GeminiImageService;
import or.sopt.houme.domain.generateImage.service.openai.facade.OpenAiFacade;
import or.sopt.houme.domain.generateImage.service.prompt.PromptService;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncGenerateImageServiceImpl implements AsyncGenerateImageService{

    // OpenAi 이미지 생성 파사드
    private final OpenAiFacade openAiFacade;
    private final PromptService promptService;
    private final GeminiImageService geminiImageService;
    private final MeterRegistry meterRegistry;

    // 비동기 이미지 생성 처리
    @Async("imageGenerationExecutor")
    @Override
    public CompletableFuture<ImageUploadResponseDTO> generateImageAsync(PromptRequestDTO promptRequestDTO) {
        return executeAsync("openai", () -> openAiFacade.makeImageByFastApi(promptRequestDTO));
    }

    @Async("imageGenerationExecutor")
    @Override
    public CompletableFuture<ImageUploadResponseDTO> generateGeminiImageAsync(PromptRequestDTO promptRequestDTO) {
        return executeAsync("gemini", () -> {
            String prompt = promptService.makePrompt(promptRequestDTO);
            return geminiImageService.createImage(prompt);
        });
    }

    private CompletableFuture<ImageUploadResponseDTO> executeAsync(
            String provider,
            Supplier<ImageUploadResponseDTO> supplier
    ) {
        long startTime = System.nanoTime();
        boolean success = false;
        try {
            ImageUploadResponseDTO response = supplier.get();
            success = true;
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("이미지 생성 중 예외발생: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        } finally {
            recordAsyncMetric(provider, success, startTime);
        }
    }

    private void recordAsyncMetric(String provider, boolean success, long startTime) {
        String result = success ? "success" : "failure";
        meterRegistry.counter(
                "houme.generate-image.async.requests.total",
                "provider", provider,
                "result", result
        ).increment();
        Timer.builder("houme.generate-image.async.duration")
                .description("이미지 생성 비동기 작업 소요 시간")
                .publishPercentileHistogram()
                .tag("provider", provider)
                .tag("result", result)
                .register(meterRegistry)
                .record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS);
    }
}
