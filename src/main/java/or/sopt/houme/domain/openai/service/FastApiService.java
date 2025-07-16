package or.sopt.houme.domain.openai.service;

import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;

public interface FastApiService {
    ImageUploadResponseDTO getImageByFastApi(PromptRequestDTO request);
}
