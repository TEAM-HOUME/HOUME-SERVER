package or.sopt.houme.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BlacklistTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "blacklist-token:";

    // 블랙리스트 토큰 저장
    public void save(String jti, long ttlSeconds) {
        String key = PREFIX + jti;
        redisTemplate.opsForValue().set(key, "logout", Duration.ofSeconds(ttlSeconds));
    }

    // 블랙리스트에 존재 여부 확인
    public boolean exists(String jti) {
        String key = PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
