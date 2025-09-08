package or.sopt.houme.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync    // 비동기 기능 활성화
@Configuration
public class AsyncConfig {

    @Bean(name = "imageGenerationExecutor")
    public Executor imageGenerationExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);        // 동시에 실행할 기본 스레드 수, JVM 기본 스레드와 별도
        executor.setMaxPoolSize(2);         // 최대 스레드 수
        executor.setQueueCapacity(5);     // 대기 큐 크기
        executor.setThreadNamePrefix("ImageGenerator-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
