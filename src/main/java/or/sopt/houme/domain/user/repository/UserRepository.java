package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);
}
