package or.sopt.houme.domain.prompt.util;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.service.OpenAiService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final OpenAiService imageService;

    @Override
    public void makePrompt() {

        String result = "프롬프트 제작 계획이 확정되면 구현합니다";

        imageService.createImage(result);
    }
}
