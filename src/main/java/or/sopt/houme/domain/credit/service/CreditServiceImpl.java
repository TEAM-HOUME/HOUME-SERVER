package or.sopt.houme.domain.credit.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CreditException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditServiceImpl implements CreditService{

    private final CreditRepository creditRepository;

    // 크레딧 감소 로직
    @Transactional
    @Override
    public void decreaseCredit(User user) {

        Credit credit = creditRepository.findOldCreditByUser(user)
                .orElseThrow(() -> new CreditException(ErrorCode.CREDIT_NOT_FOUND));

        creditRepository.delete(credit);
    }
}
