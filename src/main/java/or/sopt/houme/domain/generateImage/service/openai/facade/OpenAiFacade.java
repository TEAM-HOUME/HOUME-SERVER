package or.sopt.houme.domain.generateImage.service.openai.facade;

import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;

public interface OpenAiFacade {
    ImageUploadResponseDTO makeImage(PromptRequestDTO promptRequestDTO);

    ImageUploadResponseDTO makeImageByFastApi(PromptRequestDTO promptRequestDTO);

    ImageUploadResponseDTO testMakeImage();
}
