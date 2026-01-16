package or.sopt.houme.domain.gemini.dto;

import java.util.List;

public record GeminiImageResponse(
        List<Candidate> candidates
) {
    public record Candidate(Content content) {
    }

    public record Content(List<Part> parts) {
    }

    public record Part(InlineData inlineData, String text) {
    }

    public record InlineData(String mimeType, String data) {
    }
}
