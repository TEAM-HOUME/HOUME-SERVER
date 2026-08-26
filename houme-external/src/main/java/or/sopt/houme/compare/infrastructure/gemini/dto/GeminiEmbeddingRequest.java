package or.sopt.houme.compare.infrastructure.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiEmbeddingRequest(
        String model,
        Content content,
        Integer outputDimensionality
) {
    public static GeminiEmbeddingRequest forText(String text) {
        return new GeminiEmbeddingRequest(
                "models/gemini-embedding-2",
                new Content(List.of(new Part(text, null))),
                512
        );
    }

    public static GeminiEmbeddingRequest forImage(String mimeType, String base64Data) {
        return new GeminiEmbeddingRequest(
                "models/gemini-embedding-2",
                new Content(List.of(new Part(null, new InlineData(mimeType, base64Data)))),
                512
        );
    }

    public record Content(List<Part> parts) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Part(String text, InlineData inline_data) {}

    public record InlineData(String mime_type, String data) {}
}
