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
                "event=image.ai.request.started provider=fastapi operation=generateImage floorPlanId={} tagId={}",
                request.floorPlanId(),
                request.tagId()
        );
        ImageUploadResponseDTO response = fastApiImageClient.generateImage(request);
        log.info(
                "event=image.ai.request.succeeded provider=fastapi operation=generateImage durationMs={}",
                elapsedMillis(startTime)
        );
        return response;
    }

    public ImageUploadResponseDTO fallbackGetImageByFastApi(PromptRequestDTO request, Throwable t) {
        log.error(
                "event=image.ai.fallback provider=fastapi operation=generateImage floorPlanId={} tagId={} exceptionType={} message={}",
                request.floorPlanId(),
                request.tagId(),
                t.getClass().getSimpleName(),
                t.getMessage(),
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
