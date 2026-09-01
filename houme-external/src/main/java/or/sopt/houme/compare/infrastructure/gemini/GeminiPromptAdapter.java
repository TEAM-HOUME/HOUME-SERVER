package or.sopt.houme.compare.infrastructure.gemini;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.domain.port.out.GeminiPromptPort;
import or.sopt.houme.compare.infrastructure.gemini.client.GeminiTextGenerationClient;
import or.sopt.houme.compare.infrastructure.gemini.dto.GeminiTextGenerationRequest;
import or.sopt.houme.compare.infrastructure.gemini.dto.GeminiTextGenerationResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CompareException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiPromptAdapter implements GeminiPromptPort {

    private static final String MODEL = "gemini-3.5-flash-lite";

    private final GeminiTextGenerationClient textGenerationClient;

    @Value("${gemini.compare-api-key:}")
    private String apiKey;

    @Override
    public String generate(String prompt) {
        try {
            GeminiTextGenerationResponse response = textGenerationClient.generateContent(
                    MODEL,
                    apiKey,
                    GeminiTextGenerationRequest.of(prompt)
            );
            String responseText = response.extractText();
            if (responseText == null || responseText.isBlank()) {
                throw new CompareException(ErrorCode.COMPARE_KEYWORD_TRANSLATION_FAILED);
            }
            return responseText.trim();
        } catch (FeignException e) {
            log.error("Gemini 임시 프롬프트 호출 실패", e);
            throw new CompareException(ErrorCode.COMPARE_KEYWORD_TRANSLATION_FAILED);
        }
    }
}
