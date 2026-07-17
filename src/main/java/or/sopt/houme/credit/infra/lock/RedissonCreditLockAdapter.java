package or.sopt.houme.credit.infra.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.credit.domain.port.out.CreditLockPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CreditException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

/**
 * {@link CreditLockPort} 의 Redisson 분산 락 구현. 기존 CreditServiceImpl 락 흐름을 그대로 이관.
 *
 * <p>키 {@code credit_lock_user_{userId}}, 10초 대기 / 210초 리스(이미지 생성 시간보다 길게).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonCreditLockAdapter implements CreditLockPort {

    private static final long WAIT_SECONDS = 10;
    private static final long LEASE_SECONDS = 210;

    private final RedissonClient redissonClient;

    private RLock lock(Long userId) {
        return redissonClient.getLock("credit_lock_user_" + userId);
    }

    @Override
    public boolean tryLock(Long userId) {
        try {
            return lock(userId).tryLock(WAIT_SECONDS, LEASE_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CreditException(ErrorCode.CREDIT_LOCK_INTERRUPTED);
        }
    }

    @Override
    public void unlock(Long userId) {
        RLock lock = lock(userId);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    @Override
    public void unlockAfterCurrentTransaction(Long userId) {
        final RLock lock = lock(userId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        });
    }
}
