package or.sopt.houme.domain.generateImage.infrastructure.gemini.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiImageRequest(
        List<Content> contents,
        GenerationConfig generationConfig
) {
    public static GeminiImageRequest of(String prompt) {
        return new GeminiImageRequest(
                List.of(new Content("user", List.of(Part.text(prompt)))),
                new GenerationConfig(List.of("IMAGE"))
        );
    }

    public static GeminiImageRequest of(String prompt, List<Part> referenceImageParts) {
        List<Part> parts = new ArrayList<>();
        parts.add(Part.text(prompt));
        if (referenceImageParts != null) {
            parts.addAll(referenceImageParts);
        }
        return new GeminiImageRequest(
                List.of(new Content("user", parts)),
                new GenerationConfig(List.of("IMAGE"))
        );
    }

    public record Content(String role, List<Part> parts) {
    }

    public record Part(String text, InlineData inlineData) {
        public static Part text(String text) {
            return new Part(text, null);
        }

        public static Part inlineData(String mimeType, String data) {
            return new Part(null, new InlineData(mimeType, data));
        }
    }

    public record InlineData(String mimeType, String data) {
    }

    public record GenerationConfig(List<String> responseModalities) {
    }
}
