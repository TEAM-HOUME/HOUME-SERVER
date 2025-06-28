package or.sopt.houme.domain.openai.util;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.openai.service.ImageService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class PromptUtilImpl implements PromptUtil {

    private final ImageService imageService;

    @Override
    public void makePrompt() {

        String result = "프롬프트 제작 계획이 확정되면 구현합니다";

        imageService.createImage(result);
    }
}
