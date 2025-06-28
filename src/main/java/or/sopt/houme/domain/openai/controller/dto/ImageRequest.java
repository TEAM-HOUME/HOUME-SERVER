package or.sopt.houme.domain.openai.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequest {
    private String model = "gpt-image-1";
    private String prompt;
    private int n = 1;
    private String size = "1024x1024";
    private String quality = "medium";
    private String background = "auto";
    private String output_format = "png";
}
