package or.sopt.houme.domain.coupang.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "coupang.batch")
public class CoupangBatchProperties {

    private boolean enabled = false;
    private int searchLimit = 10;
    private long minimumCallIntervalMinutes = 7;
}
