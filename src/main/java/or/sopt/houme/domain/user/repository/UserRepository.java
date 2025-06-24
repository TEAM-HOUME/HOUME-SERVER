package or.sopt.houme.domain.user.repository;

import or.sopt.houme.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
