package or.sopt.houme.domain.openai.facade;

import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;

public interface OpenAiFacade {
    ImageUploadResponseDTO makeImage(PromptRequestDTO promptRequestDTO);

    ImageUploadResponseDTO testMakeImage();
}
