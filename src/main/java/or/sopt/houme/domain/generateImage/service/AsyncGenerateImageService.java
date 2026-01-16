package or.sopt.houme.domain.generateImage.service;

import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;

import java.util.concurrent.CompletableFuture;

public interface AsyncGenerateImageService {

    // 비동기로 이미지 생성
    CompletableFuture<ImageUploadResponseDTO> generateImageAsync(PromptRequestDTO promptRequestDTO);

    CompletableFuture<ImageUploadResponseDTO> generateGeminiImageAsync(PromptRequestDTO promptRequestDTO);
}
