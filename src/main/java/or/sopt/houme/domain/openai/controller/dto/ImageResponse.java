package or.sopt.houme.domain.openai.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImageResponse {
    private List<ImageData> data;

    @Data
    public static class ImageData {
        private String b64_json;
    }
}
