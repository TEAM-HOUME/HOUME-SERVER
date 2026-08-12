package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.user.model.entity.record.SignupSession;

import java.util.Optional;

/** 가입 세션 저장소 포트 (#582 — 구현은 Redis 어댑터). */
public interface SignupSessionRepository {

    void save(String signupToken, SignupSession session, long ttlSeconds);

    Optional<SignupSession> consume(String signupToken);
}
