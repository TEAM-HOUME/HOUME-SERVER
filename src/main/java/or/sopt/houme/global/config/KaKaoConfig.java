package or.sopt.houme.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public class KaKaoConfig {

    private String clientId;
    private String scope;
    private String redirectUri;
}
