package or.sopt.houme.domain.generateImage.presentation.openai.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class OpenAiRequest {

    private String model;
    private String prompt;
    private int n;
    private String size;
    private String quality;
    private String background;
    private String output_format;

    public static OpenAiRequest of(
            String model,
            String prompt,
            int n,
            String size,
            String quality,
            String background,
            String output_format
    ) {
        return OpenAiRequest.builder()
                .model(model)
                .prompt(prompt)
                .n(n)
                .size(size)
                .quality(quality)
                .background(background)
                .output_format(output_format)
                .build();
    }
}