package or.sopt.houme.domain.credit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import or.sopt.houme.domain.credit.entity.Credit;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CreditException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditServiceImpl implements CreditService{

    private final CreditRepository creditRepository;
    private final RedissonClient redissonClient;

    // 락으로 크레딧 처리
    @Override
    @Transactional
    public void decreaseCreditAtomically(User user) {
        // 사용자별 고유 키 생성
        final String lockKey = "credit_lock_user_" + user.getId();
        final RLock lock = redissonClient.getLock(lockKey);

        try {
            // 10초 동안 락 획득 시도, 60초 동안 잠금 유지
            boolean isLocked = lock.tryLock(10, 60, TimeUnit.SECONDS);

            // 만약 획득에 실패하면
            if (!isLocked) {
                log.error("사용자 락 획득 실패: {}", user.getId());
                throw new CreditException(ErrorCode.CREDIT_LOCK_FAILED);
            }

            // 락 획득 성공, DB 작업하기 (오래된 크레딧 조회)
            Credit credit = creditRepository.findOldCreditByUser(user)
                    .orElseThrow(() -> new CreditException(ErrorCode.CREDIT_NOT_FOUND));

            // 삭제
            creditRepository.delete(credit);

        } catch (InterruptedException e) {
            // 잠금을 기다리는 동안 스레드가 인터럽트되면, 현재 스레드의 인터럽트 상태를 다시 설정합니다.
            Thread.currentThread().interrupt();
            // 인터럽트 발생을 알리는 커스텀 예외를 던집니다.
            throw new CreditException(ErrorCode.CREDIT_LOCK_INTERRUPTED);
        } finally {
            // 작업이 성공하든 실패하든, 다른 요청의 처리를 위해 잠금을 해제해야 합니다.
            // 현재 스레드가 잠금을 보유하고 있는지 확인하고, 그렇다면 잠금을 해제합니다.
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
