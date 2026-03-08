package or.sopt.houme.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@EnableAsync    // 비동기 기능 활성화
@Configuration
public class AsyncConfig {

    @Bean(name = "imageGenerationExecutor")
    public Executor imageGenerationExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);        // 동시에 실행할 기본 스레드 수, JVM 기본 스레드와 별도
        executor.setMaxPoolSize(16);        // 최대 스레드 수
        // maxPoolSize 확장이 실제로 일어나도록 대기 큐는 더 작게 유지한다.
        executor.setQueueCapacity(8);       // 대기 큐 크기
        executor.setThreadNamePrefix("ImageGenerator-");
        // 스레드 풀이 꽉차면 남은 요청을 요청 스레드가 직접 실행해서 안정성이 높음
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 스레드 풀이 꽉차면 예외를 터뜨려 명시적 처리 가능
        executor.setRejectedExecutionHandler((r, executor1) -> {
            throw new RejectedExecutionException("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        });
        executor.initialize();
        return executor;
    }
}
