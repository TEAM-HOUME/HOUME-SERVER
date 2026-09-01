package or.sopt.houme.domain.coupang.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "coupang.partners")
public class CoupangPartnersProperties {

    private String accessKey;
    private String secretKey;
    private String baseUrl = "https://api-gateway.coupang.com";

    public boolean isConfigured() {
        return accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank();
    }
}
