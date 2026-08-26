package or.sopt.houme.compare.infrastructure.gemini.dto;

import java.util.List;

public record GeminiTextGenerationResponse(List<Candidate> candidates) {

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) return "";
        Candidate first = candidates.get(0);
        if (first.content() == null || first.content().parts() == null || first.content().parts().isEmpty()) return "";
        String text = first.content().parts().get(0).text();
        return text != null ? text : "";
    }

    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}
}
