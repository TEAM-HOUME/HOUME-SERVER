package or.sopt.houme.domain.generateImage.infrastructure.openai.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.infrastructure.openai.client.FastApiImageClient;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.constant.S3Constant;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static or.sopt.houme.global.logging.LogMarkers.fields;

@Service
@RequiredArgsConstructor
@Slf4j
public class FastApiServiceImpl implements FastApiService {

    private final FastApiImageClient fastApiImageClient;

    @Override
    @CircuitBreaker(name = "imageClient", fallbackMethod = "fallbackGetImageByFastApi")
    public ImageUploadResponseDTO getImageByFastApi(PromptRequestDTO request){
        long startTime = System.nanoTime();
        log.info(
                fields(
                        "event", "image.ai.request.started",
                        "provider", "fastapi",
                        "operation", "generateImage",
                        "floorPlanId", request.floorPlanId(),
                        "tagId", request.tagId()
                ),
                "image ai request started"
        );
        ImageUploadResponseDTO response = fastApiImageClient.generateImage(request);
        log.info(
                fields(
                        "event", "image.ai.request.succeeded",
                        "provider", "fastapi",
                        "operation", "generateImage",
                        "durationMs", elapsedMillis(startTime)
                ),
                "image ai request succeeded"
        );
        return response;
    }

    public ImageUploadResponseDTO fallbackGetImageByFastApi(PromptRequestDTO request, Throwable t) {
        log.error(
                fields(
                        "event", "image.ai.fallback",
                        "provider", "fastapi",
                        "operation", "generateImage",
                        "floorPlanId", request.floorPlanId(),
                        "tagId", request.tagId(),
                        "exceptionType", t.getClass().getSimpleName(),
                        "errorMessage", t.getMessage()
                ),
                "image ai fallback executed",
                t
        );
        return ImageUploadResponseDTO.builder()
                .filename("LLM_FAIL_FILE")
                .originalFilename("LLM_FAIL_FILE")
                .imageLink(S3Constant.FALL_BACK_IMAGE)
                .contentType("image/png")
                .build();
    }

    private long elapsedMillis(long startTime) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

}
