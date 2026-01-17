package or.sopt.houme.domain.user.repository.admin;

import or.sopt.houme.domain.user.model.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String email);
}
