package or.sopt.houme.domain.openai.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiRequest {

    private String model = "gpt-image-1";
    private String prompt;
    private int n = 1;
    private String size = "1024x1024";
    private String quality = "medium";
    private String background = "auto";
    private String output_format = "png";

    public static OpenAiRequest of(String prompt) {
        return OpenAiRequest.builder()
                .prompt(prompt)
                .model("gpt-image-1")
                .n(1)
                .size("1024x1024")
                .quality("medium")
                .background("auto")
                .output_format("png")
                .build();
    }

}
