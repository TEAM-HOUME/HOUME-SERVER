package or.sopt.houme.domain.user.repository;

import or.sopt.houme.user.infra.persistence.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserJpaEntity, Long>, UserRepositoryCustom {
    Boolean existsByEmail(String email);

    Boolean existsByNicknameAndNicknameTag(String nickname, String nicknameTag);

    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findById(Long id);

}
