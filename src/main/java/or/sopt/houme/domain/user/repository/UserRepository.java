package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    String findNameById(Long id);
}
