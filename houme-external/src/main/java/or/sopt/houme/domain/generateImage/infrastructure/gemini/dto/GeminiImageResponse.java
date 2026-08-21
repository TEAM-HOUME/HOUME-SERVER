package or.sopt.houme.domain.generateImage.infrastructure.gemini.dto;

import java.util.List;

public record GeminiImageResponse(
        List<Candidate> candidates,
        UsageMetadata usageMetadata
) {
    public record Candidate(Content content, String finishReason) {
    }

    public record Content(List<Part> parts) {
    }

    public record Part(InlineData inlineData, String text) {
    }

    public record InlineData(String mimeType, String data) {
    }

    /** Gemini 응답에 포함되는 경우에만 채워지며, 모델별로 일부 값은 null일 수 있다. */
    public record UsageMetadata(
            Integer promptTokenCount,
            Integer candidatesTokenCount,
            Integer totalTokenCount,
            Integer thoughtsTokenCount,
            Integer cachedContentTokenCount
    ) {
    }
}
