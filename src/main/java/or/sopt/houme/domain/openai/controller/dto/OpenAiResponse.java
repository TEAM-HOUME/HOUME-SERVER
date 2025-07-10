package or.sopt.houme.domain.openai.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class OpenAiResponse {
    private List<ImageData> data;

    @Getter
    @Setter
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
