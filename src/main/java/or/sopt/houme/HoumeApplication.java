package or.sopt.houme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
public class HoumeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HoumeApplication.class, args);
    }

}
