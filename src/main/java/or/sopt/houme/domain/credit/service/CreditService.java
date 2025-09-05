package or.sopt.houme.domain.credit.service;

import or.sopt.houme.domain.user.entity.User;

public interface CreditService {

    // 원자적으로 크레딧을 감소시키는 서비스
    void decreaseCreditAtomically(User user);
}