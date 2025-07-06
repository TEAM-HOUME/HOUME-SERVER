package or.sopt.houme.domain.prompt.service;

import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;

public interface PromptService {

    String makePrompt(PromptRequestDTO promptRequestDTO);
}
