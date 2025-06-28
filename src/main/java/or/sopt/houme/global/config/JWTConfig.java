package or.sopt.houme.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "spring.jwt")
public class JWTConfig {


    /**
     * JWT 설정값에 대한 검증로직을 추가했습니다
     * 여러 개발자들과의 협업과정에서의 유지보수를 고려한 리팩터링입니다
     * */
    @NotBlank(message = "JWT 헤더 설정값이 비어있습니다")
    private String header;

    @NotBlank(message = "JWT SECRET 설정값이 비어있습니다")
    @Size(min = 32, message = "JWT SECRET 은 최소 32자를 넘어야합니다")
    private String secret;

    // 초단위로 토큰의 만료기간을 설정하고 ms단위로 로직에서 파싱한다
    private Long accessTokenValidityInSeconds;
    private Long refreshTokenValidityInSeconds;

}
