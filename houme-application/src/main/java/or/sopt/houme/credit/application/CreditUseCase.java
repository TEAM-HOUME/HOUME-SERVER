package or.sopt.houme.credit.application;

import or.sopt.houme.credit.domain.CreditReservation;

/**
 * 크레딧 인바운드 포트(유스케이스). api/다른 도메인은 이 인터페이스로만 크레딧을 다룬다.
 * 웹 DTO를 노출하지 않고 원시 식별자/도메인 핸들만 주고받는다.
 */
public interface CreditUseCase {

    /** 회원에게 ACTIVE 크레딧 count 개 지급. 지급 후 ACTIVE 잔액 반환. */
    long grant(Long userId, int count);

    /** 가장 오래된 ACTIVE 크레딧 1개 즉시 차감 (원자적). */
    void deductOldestActive(Long userId);

    /** ACTIVE 1개를 PENDING 으로 예약하고 락을 건다. 이미지 생성 시작 시. */
    CreditReservation reserve(Long userId);

    /** 예약 크레딧 최종 삭제 (이미지 생성 성공). */
    void commit(CreditReservation reservation);

    /** 예약 크레딧 복구 (이미지 생성 실패). 멱등. */
    void rollback(CreditReservation reservation);

    /** 예약 시 걸어둔 락 수동 해제. */
    void releaseLock(Long userId);

    /** 현재 ACTIVE 크레딧 개수(잔액). */
    long countActive(Long userId);

    /** 회원의 전체 크레딧 삭제 (회원탈퇴). */
    void deleteAll(Long userId);
}
