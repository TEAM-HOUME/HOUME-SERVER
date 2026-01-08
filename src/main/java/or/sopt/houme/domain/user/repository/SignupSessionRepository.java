package or.sopt.houme.domain.user.repository;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.service.SignupSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SignupSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "signup-session:";

    public void save(String signupToken, SignupSession session, long ttlSeconds) {
        if (!StringUtils.hasText(signupToken) || session == null) {
            return;
        }
        String key = PREFIX + signupToken;
        redisTemplate.opsForValue().set(key, session, Duration.ofSeconds(ttlSeconds));
    }

    public Optional<SignupSession> consume(String signupToken) {
        if (!StringUtils.hasText(signupToken)) {
            return Optional.empty();
        }
        String key = PREFIX + signupToken;

        Object value;
        try {
            value = redisTemplate.opsForValue().getAndDelete(key);
        } catch (Exception e) {
            value = redisTemplate.opsForValue().get(key);
            redisTemplate.delete(key);
        }

        if (value instanceof SignupSession session) {
            return Optional.of(session);
        }
        return Optional.empty();
    }
}

