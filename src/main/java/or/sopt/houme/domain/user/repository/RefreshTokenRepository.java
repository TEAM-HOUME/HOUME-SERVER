package or.sopt.houme.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "refresh-token:";

    // userId 를 통한 키 값을 통해 리프레시 토큰이 존재하는지 탐색
    public boolean existsById(Long userId) {
        String key = PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 리프레시 토큰 저장 + TTL 설정
    public void saveRefreshToken(Long userId, String refreshToken, long ttlSeconds) {
        String key = PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(ttlSeconds));
    }

    // 리프레시 토큰 삭제
    public void deleteById(Long userId) {
        String key = PREFIX + userId;
        redisTemplate.delete(key);
    }
}
