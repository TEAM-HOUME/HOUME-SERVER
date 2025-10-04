package or.sopt.houme.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "naver")
@Getter
@Setter
@Component
@Validated
public class NaverProperties {

    @NotBlank(message = "네이버 클라이언트 ID는 필수입니다")
    private String clientId;

    @NotBlank(message = "네이버 클라이언트 시크릿은 필수입니다")
    private String clientSecret;

    @NotEmpty(message = "허용된 쇼핑몰 목록은 최소 1개 이상이어야 합니다")
    private List<String> allowedMalls;
}
