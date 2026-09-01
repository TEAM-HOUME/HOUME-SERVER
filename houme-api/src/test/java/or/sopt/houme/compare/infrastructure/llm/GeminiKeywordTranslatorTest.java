package or.sopt.houme.compare.infrastructure.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.compare.domain.MarketplaceSearchKeywords;
import or.sopt.houme.compare.infrastructure.gemini.client.GeminiTextGenerationClient;
import or.sopt.houme.compare.infrastructure.gemini.dto.GeminiTextGenerationRequest;
import or.sopt.houme.compare.infrastructure.gemini.dto.GeminiTextGenerationResponse;
import or.sopt.houme.furniture.domain.FurnitureWithTypeView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeminiKeywordTranslatorTest {

    @Test
    @DisplayName("현재 가구 후보 목록을 Gemini에 전달하고 응답 furnitureId를 마켓 검색어에 담는다")
    void translatesKeywordWithFurnitureIdFromCandidates() {
        GeminiTextGenerationClient textGenerationClient = mock(GeminiTextGenerationClient.class);
        GeminiKeywordTranslator translator = new GeminiKeywordTranslator(textGenerationClient, new ObjectMapper());
        ReflectionTestUtils.setField(translator, "apiKey", "test-api-key");
        when(textGenerationClient.generateContent(eq("gemini-3.5-flash-lite"), eq("test-api-key"), any()))
                .thenReturn(responseOf("""
                        {"ebayKeywords":"wood dining table","coupangKeywords":"원목 식탁","furnitureId":16}
                        """));
        List<FurnitureWithTypeView> candidates = List.of(
                new FurnitureWithTypeView(16L, "dining table", "식탁", 1, 3L, "테이블", "table")
        );

        MarketplaceSearchKeywords result = translator.translateToMarketplaceKeywords("브랜드 원목 식탁 1200", candidates);

        assertThat(result).isEqualTo(new MarketplaceSearchKeywords("wood dining table", "원목 식탁", 16L));
        ArgumentCaptor<GeminiTextGenerationRequest> requestCaptor = ArgumentCaptor.forClass(GeminiTextGenerationRequest.class);
        verify(textGenerationClient).generateContent(eq("gemini-3.5-flash-lite"), eq("test-api-key"), requestCaptor.capture());
        String prompt = requestCaptor.getValue().contents().getFirst().parts().getFirst().text();
        assertThat(prompt).contains("\"id\":16", "\"nameKr\":\"식탁\"", "\"furnitureId\": one candidate ID");
    }

    private GeminiTextGenerationResponse responseOf(String text) {
        return new GeminiTextGenerationResponse(List.of(
                new GeminiTextGenerationResponse.Candidate(
                        new GeminiTextGenerationResponse.Content(List.of(
                                new GeminiTextGenerationResponse.Part(text)
                        ))
                )
        ));
    }
}
