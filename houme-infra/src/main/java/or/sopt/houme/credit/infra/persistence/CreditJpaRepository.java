package or.sopt.houme.credit.infra.persistence;

import or.sopt.houme.credit.domain.CreditStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditJpaRepository extends JpaRepository<CreditJpaEntity, Long> {

    long countByUserIdAndStatus(Long userId, CreditStatus status);

    void deleteByUserId(Long userId);
}
