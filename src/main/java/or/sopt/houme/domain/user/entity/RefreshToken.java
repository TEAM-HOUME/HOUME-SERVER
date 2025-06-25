package or.sopt.houme.domain.user.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "token", timeToLive = 604800) // 7일 = 60 * 60 * 24 * 7
@AllArgsConstructor
@Getter
@ToString
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    private Long userId;

    private String refreshToken;

    public static RefreshToken of(Long userId, String refreshToken) {
        return RefreshToken.builder()
                .userId(userId)
                .refreshToken(refreshToken)
                .build();
    }
}