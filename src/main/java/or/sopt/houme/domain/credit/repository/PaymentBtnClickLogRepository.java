package or.sopt.houme.domain.credit.repository;

import or.sopt.houme.domain.credit.entity.PaymentBtnClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentBtnClickLogRepository extends JpaRepository<PaymentBtnClickLog, Long> {
}
