package or.sopt.houme.compare.infrastructure.gemini.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiTextGenerationResponseTest {

    @Test
    @DisplayName("정상 응답이면 text를 반환한다")
    void extractText_normalResponse_returnsText() {
        GeminiTextGenerationResponse resp = response("5 drawer chest dresser");
        assertThat(resp.extractText()).isEqualTo("5 drawer chest dresser");
    }

    @Test
    @DisplayName("candidates가 null이면 빈 문자열을 반환한다")
    void extractText_nullCandidates_returnsEmpty() {
        assertThat(new GeminiTextGenerationResponse(null).extractText()).isEmpty();
    }

    @Test
    @DisplayName("candidates가 비어있으면 빈 문자열을 반환한다")
    void extractText_emptyCandidates_returnsEmpty() {
        assertThat(new GeminiTextGenerationResponse(List.of()).extractText()).isEmpty();
    }

    @Test
    @DisplayName("parts가 비어있으면 빈 문자열을 반환한다")
    void extractText_emptyParts_returnsEmpty() {
        GeminiTextGenerationResponse resp = new GeminiTextGenerationResponse(
                List.of(new GeminiTextGenerationResponse.Candidate(
                        new GeminiTextGenerationResponse.Content(List.of()))));
        assertThat(resp.extractText()).isEmpty();
    }

    @Test
    @DisplayName("text가 null인 Part면 빈 문자열을 반환한다 (NPE 방어)")
    void extractText_nullText_returnsEmpty() {
        GeminiTextGenerationResponse resp = new GeminiTextGenerationResponse(
                List.of(new GeminiTextGenerationResponse.Candidate(
                        new GeminiTextGenerationResponse.Content(
                                List.of(new GeminiTextGenerationResponse.Part(null))))));
        assertThat(resp.extractText()).isEmpty();
    }

    private GeminiTextGenerationResponse response(String text) {
        return new GeminiTextGenerationResponse(
                List.of(new GeminiTextGenerationResponse.Candidate(
                        new GeminiTextGenerationResponse.Content(
                                List.of(new GeminiTextGenerationResponse.Part(text))))));
    }
}
