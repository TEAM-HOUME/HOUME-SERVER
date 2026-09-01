package or.sopt.houme.compare.presentation.controller;

import or.sopt.houme.compare.application.AdminEbaySearchService;
import or.sopt.houme.compare.application.GeminiPromptService;
import or.sopt.houme.compare.presentation.dto.request.GeminiPromptRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminCompareControllerTest {

    @Test
    @DisplayName("임시 Gemini 프롬프트 API는 모델 응답을 성공 포맷으로 반환한다")
    void generatesGeminiPrompt() {
        AdminEbaySearchService adminEbaySearchService = mock(AdminEbaySearchService.class);
        GeminiPromptService geminiPromptService = mock(GeminiPromptService.class);
        AdminCompareController controller = new AdminCompareController(adminEbaySearchService, geminiPromptService);
        when(geminiPromptService.generate("원목 식탁 특징을 알려줘")).thenReturn("원목 식탁입니다.");

        var response = controller.generateGeminiPrompt(new GeminiPromptRequest("원목 식탁 특징을 알려줘"));

        verify(geminiPromptService).generate("원목 식탁 특징을 알려줘");
        assertThat(response.getBody().data()).isEqualTo("원목 식탁입니다.");
    }
}
