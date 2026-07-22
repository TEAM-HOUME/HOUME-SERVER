package or.sopt.houme.domain.credit.service;

import or.sopt.houme.user.domain.User;

/** 결제버튼 클릭로그 기록 인바운드 계약 (#582). */
public interface PaymentBtnClickLogService {

    void createPaymentBtnClickLog(User user);
}
