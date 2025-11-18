package or.sopt.houme.domain.openai.service;

import or.sopt.houme.domain.openai.controller.dto.OpenAiRequest;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;

public interface OpenAiService {
    ImageUploadResponseDTO createImage(String prompt);

    byte[] getGptImage(OpenAiRequest request);
}
