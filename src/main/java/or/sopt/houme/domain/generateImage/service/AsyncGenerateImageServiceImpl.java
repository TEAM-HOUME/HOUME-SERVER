package or.sopt.houme.domain.generateImage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.openai.facade.OpenAiFacade;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncGenerateImageServiceImpl implements AsyncGenerateImageService{

    // OpenAi 이미지 생성 파사드
    private final OpenAiFacade openAiFacade;

    // 비동기 이미지 생성 처리
    @Async("imageGenerationExecutor")
    @Override
    public CompletableFuture<ImageUploadResponseDTO> generateImageAsync(PromptRequestDTO promptRequestDTO) {

        try {
            // 이미지 생성
            ImageUploadResponseDTO response = openAiFacade.makeImageByFastApi(promptRequestDTO);
            // 비동기 타입으로 반환
            return CompletableFuture.completedFuture(response);
        } catch (Exception e){

            // 예외 발생
            log.error("이미지 생성 중 예외발생: {}", e.getMessage());
            // 비동기 작업 내에서 발생한 예외를 CompletableFuture에 담아 반환
            return CompletableFuture.failedFuture(e);
        }

    }
}
