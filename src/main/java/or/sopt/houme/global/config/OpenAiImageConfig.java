package or.sopt.houme.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "openai.image")
public class OpenAiImageConfig {
    private String model;
    private int n;
    private String size;
    private String quality;
    private String background;
    private String outputFormat;
}
