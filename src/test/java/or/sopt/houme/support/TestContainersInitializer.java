package or.sopt.houme.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * test 프로파일의 모든 스프링 컨텍스트에 실제 PostgreSQL 16 / Redis 7 컨테이너를 공급한다.
 *
 * <p>application-test.yml 의 {@code context.initializer.classes} 로 등록되므로,
 * {@link IntegrationTestSupport} 상속 여부와 무관하게 test 프로파일로 컨텍스트를 띄우는
 * 모든 테스트(기존 @SpringBootTest 포함)가 동일한 인프라를 공유한다.
 * 덕분에 CI 시크릿(APPLICATION_TEST)이나 로컬 Redis 설치 없이 어디서든 테스트가 돈다.
 *
 * <p>컨테이너는 JVM 당 한 번만 기동되는 static 싱글톤이며 Ryuk 이 테스트 종료 후 정리한다.
 */
public class TestContainersInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("houme")
                    .withUsername("houme")
                    .withPassword("houme");

    private static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    static {
        POSTGRES.start();
        REDIS.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        TestPropertyValues.of(
                "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                "spring.datasource.username=" + POSTGRES.getUsername(),
                "spring.datasource.password=" + POSTGRES.getPassword(),
                "spring.data.redis.host=" + REDIS.getHost(),
                "spring.data.redis.port=" + REDIS.getMappedPort(6379)
        ).applyTo(context.getEnvironment());
    }
}
