package or.sopt.houme.credit.domain.port.out;

/**
 * 크레딧 동시성 제어 아웃바운드 포트. 구현은 infra 의 분산 락(Redisson) 어댑터.
 *
 * <p>사용자별 락으로 지급/차감/예약이 경합하지 않도록 보장한다.
 */
public interface CreditLockPort {

    /** 사용자 락 획득 시도. 실패 시 false. */
    boolean tryLock(Long userId);

    /** 현재 스레드가 보유한 락 해제. */
    void unlock(Long userId);

    /** 현재 트랜잭션 완료(commit/rollback) 후 자동으로 락을 해제하도록 등록. */
    void unlockAfterCurrentTransaction(Long userId);
}
