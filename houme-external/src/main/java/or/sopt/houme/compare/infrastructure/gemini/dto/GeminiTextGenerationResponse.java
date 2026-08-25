package or.sopt.houme.compare.infrastructure.gemini.dto;

import java.util.List;

public record GeminiTextGenerationResponse(List<Candidate> candidates) {

    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) return "";
        Candidate c = candidates.get(0);
        if (c.content() == null || c.content().parts() == null || c.content().parts().isEmpty()) return "";
        return c.content().parts().get(0).text();
    }
}
