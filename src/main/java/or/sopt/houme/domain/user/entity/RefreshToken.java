package or.sopt.houme.domain.user.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "token", timeToLive = 604800) // 7일 = 60 * 60 * 24 * 7
@AllArgsConstructor
@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    private Long id;

    private String refreshToken;
}