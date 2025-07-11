package or.sopt.houme.domain.credit.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.entity.PaymentBtnClickLog;
import or.sopt.houme.domain.credit.repository.PaymentBtnClickLogRepository;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentBtnClickLogService {
    private final PaymentBtnClickLogRepository paymentBtnClickLogRepository;

    public void createPaymentBtnClickLog(User user) {
        PaymentBtnClickLog paymentBtnClickLog = PaymentBtnClickLog.of(user);
        paymentBtnClickLogRepository.save(paymentBtnClickLog);
    }
}
