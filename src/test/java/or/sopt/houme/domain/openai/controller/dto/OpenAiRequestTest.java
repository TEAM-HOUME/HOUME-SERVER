package or.sopt.houme.domain.openai.controller.dto;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiRequestTest {

    @Test
    @DisplayName("OpenAiRequest.of()로 객체 생성이 가능해야 한다")
    void testOfMethod() {
        // Given
        String model = "chatgpt-image-1";
        String prompt = "A serene mountain landscape";
        int n = 1;
        String size = "1024x1024";
        String quality = "standard";
        String background = "transparent";
        String outputFormat = "png";

        // When
        OpenAiRequest request = OpenAiRequest.of(
                model, prompt, n, size, quality, background, outputFormat
        );

        // Then
        assertThat(request.getModel()).isEqualTo(model);
        assertThat(request.getPrompt()).isEqualTo(prompt);
        assertThat(request.getN()).isEqualTo(n);
        assertThat(request.getSize()).isEqualTo(size);
        assertThat(request.getQuality()).isEqualTo(quality);
        assertThat(request.getBackground()).isEqualTo(background);
        assertThat(request.getOutput_format()).isEqualTo(outputFormat);
    }

    @Test
    @DisplayName("Builder를 사용해 OpenAiRequest 객체 생성이 가능해야 한다")
    void testBuilderMethod() {
        OpenAiRequest request = OpenAiRequest.builder()
                .model("model-x")
                .prompt("some prompt")
                .n(3)
                .size("512x512")
                .quality("hd")
                .background("white")
                .output_format("jpeg")
                .build();

        assertThat(request.getModel()).isEqualTo("model-x");
        assertThat(request.getPrompt()).isEqualTo("some prompt");
        assertThat(request.getN()).isEqualTo(3);
        assertThat(request.getSize()).isEqualTo("512x512");
        assertThat(request.getQuality()).isEqualTo("hd");
        assertThat(request.getBackground()).isEqualTo("white");
        assertThat(request.getOutput_format()).isEqualTo("jpeg");
    }
}
