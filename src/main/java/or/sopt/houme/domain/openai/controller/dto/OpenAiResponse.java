package or.sopt.houme.domain.openai.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OpenAiResponse {
    private List<ImageData> data;

    @Data
    public static class ImageData {
        private String b64_json;
    }
}
