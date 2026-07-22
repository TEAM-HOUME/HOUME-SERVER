package or.sopt.houme.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@Profile("!test") // 테스트 프로파일일 땐 로딩 막기
@EnableJpaAuditing
public class JpaAuditingConfig {
}