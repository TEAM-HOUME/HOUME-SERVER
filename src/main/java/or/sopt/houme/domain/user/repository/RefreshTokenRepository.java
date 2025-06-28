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

    /**
     * 주어진 사용자 ID에 해당하는 리프레시 토큰이 Redis에 존재하는지 확인합니다.
     *
     * @param userId 리프레시 토큰 존재 여부를 확인할 사용자 ID
     * @return 리프레시 토큰이 존재하면 true, 그렇지 않으면 false
     */
    public boolean existsById(Long userId) {
        String key = PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 지정한 사용자 ID에 대해 리프레시 토큰을 Redis에 저장하고, 주어진 TTL(초)만큼 유효 기간을 설정합니다.
     *
     * @param userId 리프레시 토큰을 저장할 사용자 ID
     * @param refreshToken 저장할 리프레시 토큰 값
     * @param ttlSeconds 토큰의 만료 시간을 초 단위로 지정
     */
    public void saveRefreshToken(Long userId, String refreshToken, long ttlSeconds) {
        String key = PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 지정된 사용자 ID에 해당하는 리프레시 토큰을 Redis에서 삭제합니다.
     *
     * @param userId 리프레시 토큰을 삭제할 사용자 ID
     */
    public void deleteById(Long userId) {
        String key = PREFIX + userId;
        redisTemplate.delete(key);
    }
}
