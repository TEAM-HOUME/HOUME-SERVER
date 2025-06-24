package or.sopt.houme.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "spring.jwt")
public class JWTConfig {

    private String header;
    private String secret;

    // 초단위로 토큰의 만료기간을 설정하고 ms단위로 로직에서 파싱한다
    private Long accessTokenValidityInSeconds;
    private Long refreshTokenValidityInSeconds;

}
