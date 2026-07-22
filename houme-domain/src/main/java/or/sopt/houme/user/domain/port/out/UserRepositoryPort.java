package or.sopt.houme.user.domain.port.out;

import or.sopt.houme.user.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * 유저 영속화 아웃바운드 포트. 도메인/애플리케이션은 이 인터페이스만 알고,
 * 구현(JPA·QueryDSL)은 infra 어댑터가 제공한다.
 */
public interface UserRepositoryPort {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNicknameAndNicknameTag(String nickname, String nicknameTag);

    /** 신규(id null)면 INSERT, 기존이면 변경 필드 반영. 저장 결과(id 채워짐)를 반환. */
    User save(User user);

    /**
     * save 후 즉시 flush. 닉네임태그 유니크 제약 위반을 호출 지점에서 바로
     * {@code DataIntegrityViolationException} 으로 받아 재시도하는 흐름(REQUIRES_NEW) 전용.
     */
    User saveAndFlush(User user);

    void deleteById(Long id);

    /** 이메일 완전일치 또는 닉네임 부분일치로 회원 검색 (id 오름차순, limit 제한) — 어드민용. */
    List<User> searchMembers(String keyword, int limit);
}
