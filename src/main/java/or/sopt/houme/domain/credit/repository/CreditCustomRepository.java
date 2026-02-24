package or.sopt.houme.domain.credit.repository;

import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.model.entity.CreditStatus;
import or.sopt.houme.domain.user.model.entity.User;

import java.util.Optional;

public interface CreditCustomRepository {

    // User로 제일 오래된 Credit 찾기
    Optional<Credit> findOldCreditByUser(User user);

    // User와 Status로 제일 오래된 Credit 찾기
    Optional<Credit> findOldCreditByUserAndStatus(User user, CreditStatus status);
}
