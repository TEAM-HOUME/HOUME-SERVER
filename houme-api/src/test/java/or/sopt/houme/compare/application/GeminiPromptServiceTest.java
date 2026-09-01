package or.sopt.houme.compare.application;

import or.sopt.houme.compare.domain.port.out.GeminiPromptPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeminiPromptServiceTest {

    @Test
    @DisplayName("임시 프롬프트를 Gemini 포트에 전달하고 응답을 반환한다")
    void generatesPromptResponse() {
        GeminiPromptPort geminiPromptPort = mock(GeminiPromptPort.class);
        GeminiPromptService geminiPromptService = new GeminiPromptService(geminiPromptPort);
        when(geminiPromptPort.generate("식탁의 특징을 한 문장으로 알려줘")).thenReturn("원목 식탁입니다.");

        String result = geminiPromptService.generate("식탁의 특징을 한 문장으로 알려줘");

        verify(geminiPromptPort).generate("식탁의 특징을 한 문장으로 알려줘");
        assertThat(result).isEqualTo("원목 식탁입니다.");
    }
}
