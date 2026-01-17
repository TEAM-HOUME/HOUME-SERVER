package or.sopt.houme.domain.generateImage.service.prompt;

import or.sopt.houme.domain.generateImage.service.prompt.dto.PromptRequestDTO;

public interface PromptService {

    String makePrompt(PromptRequestDTO promptRequestDTO);
}
