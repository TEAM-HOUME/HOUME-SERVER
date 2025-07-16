package or.sopt.houme.domain.openai.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.openai.client.FastApiImageClient;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FastApiServiceImpl implements FastApiService {

    private final FastApiImageClient fastApiImageClient;

    @Override
    @CircuitBreaker(name = "imageClient", fallbackMethod = "fallbackGetImageByFastApi")
    public ImageUploadResponseDTO getImageByFastApi(PromptRequestDTO request){

        return fastApiImageClient.generateImage(request);
    }

    public ImageUploadResponseDTO fallbackGetImageByFastApi(PromptRequestDTO request, Throwable t) {
        log.error("Fallback triggered for FastAPI image generation: {}", t.getMessage(), t);
        return ImageUploadResponseDTO.builder()
                .filename("LLM_FAIL_FILE")
                .originalFilename("LLM_FAIL_FILE")
                .imageLink("https://houme-bucket.s3.ap-northeast-2.amazonaws.com/feign_fallback/fallback_image.png")
                .contentType("image/png")
                .build();
    }

}
