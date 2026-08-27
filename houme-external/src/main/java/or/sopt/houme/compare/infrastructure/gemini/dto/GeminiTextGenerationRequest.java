package or.sopt.houme.compare.infrastructure.gemini.dto;

import java.util.List;

public record GeminiTextGenerationRequest(List<Content> contents) {

    public static GeminiTextGenerationRequest of(String text) {
        return new GeminiTextGenerationRequest(
                List.of(new Content("user", List.of(new Part(text))))
        );
    }

    public record Content(String role, List<Part> parts) {}

    public record Part(String text) {}
}
