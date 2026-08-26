package or.sopt.houme.compare.infrastructure.gemini.dto;

import java.util.List;

public record GeminiTextGenerationRequest(List<Content> contents) {

    public static GeminiTextGenerationRequest of(String prompt) {
        return new GeminiTextGenerationRequest(
                List.of(new Content(List.of(new Part(prompt))))
        );
    }

    public record Content(List<Part> parts) {}

    public record Part(String text) {}
}
