package or.sopt.houme.domain.credit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CreditException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditServiceImpl implements CreditService{

    private final CreditRepository creditRepository;
    private final RedissonClient redissonClient;

    @Override
    @Transactional
    public void decreaseCreditAtomically(User user) {
        final String lockKey = "credit_lock_user_" + user.getId();
        final RLock lock = redissonClient.getLock(lockKey);

        try {
            // 10초 동안 잠금 획득 시도, 60초 동안 잠금 유지
            boolean isLocked = lock.tryLock(10, 60, TimeUnit.SECONDS);

            if (!isLocked) {
                log.error("락 획득 실패, user: {}", user.getId());
                throw new CreditException(ErrorCode.CREDIT_LOCK_FAILED);
            }

            // 트랜잭션 완료 후 락 해제 로직 등록 (트랜잭션의 동기화를 위한 로직)
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // 트랜잭션이 commit이든 rollback이든 상관없이 실행
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        // 락 해제
                        lock.unlock();
                    }
                }
            });

            // 잠금 획득 성공, DB 작업 수행
            Credit credit = creditRepository.findOldCreditByUser(user)
                    .orElseThrow(() -> new CreditException(ErrorCode.CREDIT_NOT_FOUND));

            creditRepository.delete(credit);

        } catch (InterruptedException e) {
            // 잠금을 기다리는 동안 스레드가 인터럽트되면, 현재 스레드의 인터럽트 상태를 다시 설정
            Thread.currentThread().interrupt();
            // 인터럽트 발생을 알리는 커스텀 예외 던지기
            throw new CreditException(ErrorCode.CREDIT_LOCK_INTERRUPTED);
        }
    }
}