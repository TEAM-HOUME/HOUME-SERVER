package or.sopt.houme.domain.admin.repository;

import or.sopt.houme.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}
