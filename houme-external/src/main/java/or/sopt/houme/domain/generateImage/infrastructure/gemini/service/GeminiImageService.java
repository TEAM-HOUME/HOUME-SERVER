package or.sopt.houme.domain.generateImage.infrastructure.gemini.service;

import or.sopt.houme.global.dto.ImageUploadResponseDTO;

import java.util.List;

public interface GeminiImageService {
    ImageUploadResponseDTO createImage(String prompt);
    ImageUploadResponseDTO createImageWithReferences(String prompt, List<String> referenceImageUrls);
}
