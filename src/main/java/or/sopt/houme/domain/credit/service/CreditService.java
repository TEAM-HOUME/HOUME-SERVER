package or.sopt.houme.domain.credit.service;

import or.sopt.houme.domain.user.entity.User;

public interface CreditService {

    // 크레딧 감소 서비스
    void decreaseCredit(User user);
}
