package or.sopt.houme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HoumeApplication {

    /**
     * Spring Boot 애플리케이션을 시작하는 메인 진입점입니다.
     *
     * @param args 커맨드라인 인자 배열
     */
    public static void main(String[] args) {
        SpringApplication.run(HoumeApplication.class, args);
    }

}
