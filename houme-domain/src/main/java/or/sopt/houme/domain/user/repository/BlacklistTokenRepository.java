package or.sopt.houme.domain.user.repository;

/** 액세스 토큰 블랙리스트 저장소 포트 (#582 — 구현은 Redis 어댑터). */
public interface BlacklistTokenRepository {

    void save(String jti, long ttlSeconds);

    boolean exists(String jti);
}
