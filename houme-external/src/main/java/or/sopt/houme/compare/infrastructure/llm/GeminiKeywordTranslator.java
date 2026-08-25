package or.sopt.houme.compare.infrastructure.llm;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class GeminiKeywordTranslator {

    private static final String TRANSLATION_MODEL = "gemini-3.5-flash-lite";

    private final GeminiTextGenerationClient textGenerationClient;

    @Value("${gemini.compare-api-key:}")
    private String apiKey;

    public String translateToEnglish(String koreanProductName) {
        String prompt = """
                Extract eBay search keywords from this Korean furniture/home product name.
                Rules:
                - Output ONLY 2-4 generic English keywords describing the product type
                - Do NOT include brand names, model numbers, or sizes (like 800, 1200)
                - Focus on the product category and key features (e.g. material, style, number of drawers)
                - Example: "플렌토 속 깊은 5단 서랍장 800" → "5 drawer chest dresser"
                - Example: "린넨 2인용 소파" → "linen 2 seater sofa"
                Product: """ + koreanProductName;
        try {
            GeminiTextGenerationRequest req = GeminiTextGenerationRequest.of(prompt);
            GeminiTextGenerationResponse resp = textGenerationClient.generateContent(TRANSLATION_MODEL, apiKey, req);
            String result = resp.extractText().trim();
            if (result.isBlank()) {
                log.error("키워드 번역 결과가 비어있음: product={}", koreanProductName);
                throw new CompareException(ErrorCode.COMPARE_KEYWORD_TRANSLATION_FAILED);
            }
            log.debug("키워드 번역: '{}' → '{}'", koreanProductName, result);
            return result;
        } catch (FeignException e) {
            log.error("키워드 번역 실패: product={}", koreanProductName, e);
            throw new CompareException(ErrorCode.COMPARE_KEYWORD_TRANSLATION_FAILED);
        }
    }
}
