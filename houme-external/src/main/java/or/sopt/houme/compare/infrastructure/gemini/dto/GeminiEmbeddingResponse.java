package or.sopt.houme.compare.infrastructure.gemini.dto;

import java.util.List;

public record GeminiEmbeddingResponse(Embedding embedding) {
    public record Embedding(List<Double> values) {}
}
