package or.sopt.houme.domain.gemini.service;

import or.sopt.houme.global.dto.ImageUploadResponseDTO;

public interface GeminiImageService {
    ImageUploadResponseDTO createImage(String prompt);
}
