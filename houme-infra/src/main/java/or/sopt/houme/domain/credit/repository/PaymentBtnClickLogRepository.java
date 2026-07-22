package or.sopt.houme.domain.credit.repository;

import or.sopt.houme.domain.credit.model.entity.PaymentBtnClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentBtnClickLogRepository extends JpaRepository<PaymentBtnClickLog, Long> {
    void deleteByUserId(Long userId);
}
