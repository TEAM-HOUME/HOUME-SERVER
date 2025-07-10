package or.sopt.houme.domain.openai.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class OpenAiResponse {
    private List<ImageData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageData {
        private String url;
        private String revised_prompt;
        private String b64_json;

        @Override
        public String toString() {
            return "ImageData{" +
                    "url='" + url + '\'' +
                    ", revised_prompt='" + revised_prompt + '\'' +
                    ", b64_json='" + b64_json + '\'' +
                    '}';
        }
    }
}
