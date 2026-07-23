package or.sopt.houme.domain.generateImage.infrastructure.openai.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.generateImage.infrastructure.openai.client.FastApiImageClient;
import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import or.sopt.houme.global.util.constant.S3Constant;
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
                .imageLink(S3Constant.FALL_BACK_IMAGE)
                .contentType("image/png")
                .build();
    }

}
