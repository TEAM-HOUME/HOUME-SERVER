package or.sopt.houme.domain.generateImage.infrastructure.gemini.service;

import or.sopt.houme.global.dto.ImageUploadResponseDTO;

public interface GeminiImageService {
    ImageUploadResponseDTO createImage(String prompt);
}
