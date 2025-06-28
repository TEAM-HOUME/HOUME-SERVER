package or.sopt.houme.domain.openai.facade;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.service.OpenAiService;
import or.sopt.houme.domain.prompt.util.PromptService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenAiFacade {

    private final OpenAiService openAiService;
    private final PromptService promptService;


}
