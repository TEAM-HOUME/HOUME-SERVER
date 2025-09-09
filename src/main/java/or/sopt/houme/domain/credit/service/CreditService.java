package or.sopt.houme.domain.credit.service;

import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.user.entity.User;

public interface CreditService {

    // 원자적으로 크레딧을 감소시키는 서비스
    void decreaseCreditAtomically(User user);

    // 락 획득 및 크레딧 상태 PENDING 으로 변경
    Credit tryLockAndGetCredit(User user);

    // 크레딧 최종 삭제 (이미지 생성 성공 시)
    void commitCreditDeletion(Credit credit);

    // 크레딧 상태 복구 (이미지 생성 실패 시)
    void rollbackCreditPending(Credit credit);

    // 락 수동 해제 메서드 추가
    void releaseLock(User user);
}