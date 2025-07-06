package or.sopt.houme.domain.prompt.util;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.service.OpenAiService;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptServiceImpl implements PromptService {

    private final OpenAiService imageService;

    @Override
    public String makePrompt(PromptRequestDTO requestDTO) {

        String result = "프롬프트 제작 계획이 확정되면 구현합니다";

        // 임시로 프롬프트를 넣어보았습니다. 원래는 requestDTO를 기반으로 프롬프트를 만들어서 이를 반환합니다 FIXME
        return "웃는 남자의 이미지를 뽑아줘";
    }
}
