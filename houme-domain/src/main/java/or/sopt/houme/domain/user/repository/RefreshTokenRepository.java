package or.sopt.houme.domain.user.repository;

/** 리프레시 토큰 저장소 포트 (#582 — 구현은 Redis 어댑터). */
public interface RefreshTokenRepository {

    boolean existsById(Long userId);

    void saveRefreshToken(Long userId, String refreshToken, long ttlSeconds);

    void deleteById(Long userId);
}
