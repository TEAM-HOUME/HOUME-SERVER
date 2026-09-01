package or.sopt.houme.compare.infrastructure.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.compare.domain.MarketplaceSearchKeywords;
import or.sopt.houme.compare.domain.port.out.KeywordTranslationPort;
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
public class GeminiKeywordTranslator implements KeywordTranslationPort {

    private static final String TRANSLATION_MODEL = "gemini-3.5-flash-lite";

    private final GeminiTextGenerationClient textGenerationClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.compare-api-key:}")
    private String apiKey;

    public record KeywordPair(String english, String korean) {}

    private record KeywordJson(String ebayKeywords, String coupangKeywords) {}

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

    @Override
    public MarketplaceSearchKeywords translateToMarketplaceKeywords(String koreanProductName) {
        KeywordPair keywordPair = translateToBoth(koreanProductName);
        return new MarketplaceSearchKeywords(keywordPair.english(), keywordPair.korean());
    }

    public KeywordPair translateToBoth(String koreanProductName) {
        String prompt = buildDualKeywordPrompt(koreanProductName);
        try {
            GeminiTextGenerationRequest req = GeminiTextGenerationRequest.of(prompt);
            GeminiTextGenerationResponse resp = textGenerationClient.generateContent(TRANSLATION_MODEL, apiKey, req);
            String raw = stripCodeFence(resp.extractText().trim());

            KeywordJson parsed;
            try {
                parsed = objectMapper.readValue(raw, KeywordJson.class);
            } catch (JsonProcessingException e) {
                log.error("키워드 JSON 파싱 실패: product={}, raw={}", koreanProductName, raw);
                throw new CompareException(ErrorCode.COMPARE_KEYWORD_TRANSLATION_FAILED);
            }

            if (parsed.ebayKeywords() == null || parsed.ebayKeywords().isBlank()
                    || parsed.coupangKeywords() == null || parsed.coupangKeywords().isBlank()) {
                log.error("키워드 결과가 비어있음: product={}, raw={}", koreanProductName, raw);
                throw new CompareException(ErrorCode.COMPARE_KEYWORD_TRANSLATION_FAILED);
            }

            log.debug("키워드 번역: '{}' → en='{}', ko='{}'", koreanProductName, parsed.ebayKeywords(), parsed.coupangKeywords());
            return new KeywordPair(parsed.ebayKeywords().trim(), parsed.coupangKeywords().trim());
        } catch (FeignException e) {
            log.error("키워드 번역 실패: product={}", koreanProductName, e);
            throw new CompareException(ErrorCode.COMPARE_KEYWORD_TRANSLATION_FAILED);
        }
    }

    private String buildDualKeywordPrompt(String koreanProductName) {
        return """
                You are extracting search keywords for a Korean furniture/home-goods product,
                to be used on two different marketplaces: eBay (English) and Coupang (Korean).

                GOAL: The keywords must match SIMILAR products from OTHER sellers — not just
                this exact listing. So remove anything that only identifies THIS ONE SKU.

                REMOVE:
                - Brand / manufacturer / shop names (e.g. "플렌토", "룬드")
                - Model numbers or product codes (e.g. "800", "K-2201")
                - Exact size/dimension codes that are catalog-specific (e.g. "SS Q 슈퍼싱글 퀸", "1200x600")
                - Marketing words (e.g. "프리미엄", "인기", "베스트", "특가")

                KEEP (these describe what the product actually IS, not which SKU it is):
                - Product type/category (침대 프레임, 서랍장, 펜던트 조명, 소파 커버)
                - Material (원목, 린넨, 라탄, 패브릭)
                - Structural/functional descriptors that generalize across sellers
                  (5단, 2인용, 무헤드, 접이식, 방수)

                OUTPUT: valid JSON only. No explanation, no markdown code fence, no extra text.
                {"ebayKeywords": "2-4 generic English words", "coupangKeywords": "2-4 generic Korean words"}

                Examples:
                Input: "플렌토 속 깊은 5단 서랍장 800"
                Output: {"ebayKeywords": "5 drawer chest dresser", "coupangKeywords": "5단 서랍장"}

                Input: "룬드 무헤드 수납 침대 프레임 SS Q 슈퍼싱글 퀸"
                Output: {"ebayKeywords": "storage bed frame no headboard", "coupangKeywords": "무헤드 수납 침대 프레임"}

                Input: "노르딕 라탄 펜던트 조명 1등 골드"
                Output: {"ebayKeywords": "rattan pendant light", "coupangKeywords": "라탄 펜던트 조명"}

                Input: "린넨 2인용 소파 커버 방수 아이보리"
                Output: {"ebayKeywords": "linen sofa cover waterproof", "coupangKeywords": "린넨 소파 커버 방수"}

                Product: """ + koreanProductName;
    }

    private String stripCodeFence(String raw) {
        return raw.replaceAll("(?s)```json\\s*|```", "").trim();
    }
}
