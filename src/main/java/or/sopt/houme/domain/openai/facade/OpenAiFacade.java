package or.sopt.houme.domain.openai.facade;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.service.OpenAiService;
import or.sopt.houme.domain.prompt.dto.PromptRequestDTO;
import or.sopt.houme.domain.prompt.service.PromptService;
import or.sopt.houme.global.dto.ImageUploadResponseDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenAiFacade {

    private final OpenAiService openAiService;
    private final PromptService promptService;

    /**
     * 1. DTO 를 통해서 이미지 생성에 필요한 데이터를 받습니다
     * 2. 그러면 해당 데이터를 파싱하여 프롬프팅하고
     * 3. 그 결과를 Chatgpt-image-1 모델에 넣어 이미지를 반환합니다
     *
     * 결론적으로 채륜님이 만드는 API 로직 중 해당 메서드만 호출하면
     *
     * 0. 프롬프트 제작
     * 1. 생성형 이미지 생성하고 반환
     * 2. S3에 이미지 저장
     * 4. 데이터베이스에 이미지 저장
     *
     * 까지의 로직을 할 수 있도록 구현하였습니다
     *
     * 최종적으로는 이미지의 다양한 메타데이터를 포함한 response 를 반환합니다
     * */
    public ImageUploadResponseDTO makeImage(PromptRequestDTO promptRequestDTO){

        String prompt = promptService.makePrompt(promptRequestDTO);

        return openAiService.createImage(prompt);
    }
}
