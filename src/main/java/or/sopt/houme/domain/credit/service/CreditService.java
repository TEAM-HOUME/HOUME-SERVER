package or.sopt.houme.domain.credit.service;

import or.sopt.houme.domain.user.entity.User;

public interface CreditService {

    // 크레딧 사전 확인
    void checkUserCredit(User user);

    // 크레딧 감소 서비스
    void decreaseCredit(User user);
}
