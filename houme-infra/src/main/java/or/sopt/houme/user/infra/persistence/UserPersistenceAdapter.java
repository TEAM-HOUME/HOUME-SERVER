package or.sopt.houme.user.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.user.domain.User;
import or.sopt.houme.user.domain.port.out.UserRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link UserRepositoryPort} 의 JPA/QueryDSL 구현 어댑터.
 * 기존 {@link UserRepository}(JpaRepository)를 재사용하고, 경계를 넘을 때만 순수 도메인으로 매핑한다.
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return Boolean.TRUE.equals(userRepository.existsByEmail(email));
    }

    @Override
    public boolean existsByNicknameAndNicknameTag(String nickname, String nicknameTag) {
        return Boolean.TRUE.equals(userRepository.existsByNicknameAndNicknameTag(nickname, nicknameTag));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return UserMapper.toDomain(userRepository.save(UserMapper.toNewEntity(user)));
        }
        // 기존 유저: 조회 후 변경 필드만 반영 (트랜잭션 내 더티 체킹으로 UPDATE). 못 찾으면 fail-fast.
        UserJpaEntity entity = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("상태를 갱신할 유저를 찾을 수 없습니다. id=" + user.getId()));
        entity.applyDomainState(user);
        return UserMapper.toDomain(entity);
    }

    @Override
    public User saveAndFlush(User user) {
        // 유니크 제약 위반을 호출 지점에서 즉시 감지해야 하는 흐름(닉네임태그 재시도) 전용 — 기존 saveAndFlush 타이밍 보존
        if (user.getId() == null) {
            return UserMapper.toDomain(userRepository.saveAndFlush(UserMapper.toNewEntity(user)));
        }
        UserJpaEntity entity = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("상태를 갱신할 유저를 찾을 수 없습니다. id=" + user.getId()));
        entity.applyDomainState(user);
        return UserMapper.toDomain(userRepository.saveAndFlush(entity));
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> searchMembers(String keyword, int limit) {
        return userRepository.searchMembers(keyword, limit).stream()
                .map(UserMapper::toDomain)
                .toList();
    }
}
