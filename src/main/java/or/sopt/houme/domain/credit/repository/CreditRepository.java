package or.sopt.houme.domain.credit.repository;

import or.sopt.houme.domain.credit.entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditRepository extends JpaRepository<Credit, Long> {
}
