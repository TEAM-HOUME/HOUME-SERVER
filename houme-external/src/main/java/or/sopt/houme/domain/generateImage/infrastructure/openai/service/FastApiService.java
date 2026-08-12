package or.sopt.houme.domain.generateImage.infrastructure.openai.service;

import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;

public interface FastApiService {
    ImageUploadResponseDTO getImageByFastApi(PromptRequestDTO request);
}
