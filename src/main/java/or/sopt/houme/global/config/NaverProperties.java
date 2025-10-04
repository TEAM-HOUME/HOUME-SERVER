package or.sopt.houme.global.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "naver")
@Getter
@Setter
public class NaverProperties {

    private String clientId;
    private String clientSecret;
    private List<String> allowedMalls;

    @PostConstruct
    private void validate() {
        if (allowedMalls == null || allowedMalls.isEmpty()) {
            throw new IllegalStateException("naver.allowed-malls 설정이 비어있습니다.");
        }
    }
}
