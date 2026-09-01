package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.port.out.GeminiPromptPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiPromptService {

    private final GeminiPromptPort geminiPromptPort;

    public String generate(String prompt) {
        return geminiPromptPort.generate(prompt);
    }
}
