package or.sopt.houme.global.config;

import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRedisConfig {

    @Bean
    public RedissonClient redissonClient() {
        // Mockito를 사용하여 RedissonClient의 가짜 객체 생성
        // 이 객체는 실제 Redis 연결을 시도 X
        return Mockito.mock(RedissonClient.class);
    }
}