package or.sopt.houme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class HoumeApplication {

    /**
     * Spring Boot 애플리케이션을 시작하는 메인 엔트리 포인트입니다.
     *
     * @param args 커맨드라인 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(HoumeApplication.class, args);
    }

}
