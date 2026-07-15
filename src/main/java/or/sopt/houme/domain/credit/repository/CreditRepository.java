package or.sopt.houme.domain.credit.repository;

import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.model.entity.CreditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long>, CreditCustomRepository {
    void deleteByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, CreditStatus status);
}
