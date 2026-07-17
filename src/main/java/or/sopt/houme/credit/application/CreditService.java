package or.sopt.houme.credit.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.credit.domain.Credit;
import or.sopt.houme.credit.domain.CreditReservation;
import or.sopt.houme.credit.domain.CreditStatus;
import or.sopt.houme.credit.domain.port.out.CreditLockPort;
import or.sopt.houme.credit.domain.port.out.CreditRepositoryPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CreditException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

/**
 * 크레딧 유스케이스 구현. 도메인 규칙은 {@link Credit} 이, 영속화/락은 아웃바운드 포트가 담당하고
 * 여기서는 트랜잭션·락 획득/해제 흐름만 오케스트레이션한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CreditService implements CreditUseCase {

    private final CreditRepositoryPort creditRepository;
    private final CreditLockPort creditLock;

    @Override
    @Transactional
    public long grant(Long userId, int count) {
        try {
            List<Credit> newCredits = IntStream.range(0, count)
                    .mapToObj(i -> Credit.issue(userId))
                    .toList();
            creditRepository.saveAll(newCredits);
        } catch (Exception e) {
            throw new CreditException(ErrorCode.CREDIT_CREATE_EXCEPTION);
        }
        return creditRepository.countByUserIdAndStatus(userId, CreditStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void deductOldestActive(Long userId) {
        acquireLockOrThrow(userId);
        creditLock.unlockAfterCurrentTransaction(userId);

        Credit credit = creditRepository.findOldestByUserIdAndStatus(userId, CreditStatus.ACTIVE)
                .orElseThrow(() -> new CreditException(ErrorCode.CREDIT_NOT_FOUND));

        creditRepository.deleteById(credit.getId());
    }

    @Override
    @Transactional
    public CreditReservation reserve(Long userId) {
        acquireLockOrThrow(userId);

        Credit credit = creditRepository.findOldestByUserIdAndStatus(userId, CreditStatus.ACTIVE)
                .orElseGet(() -> {
                    // 사용 가능한 크레딧이 없으면 즉시 락 해제 후 예외
                    creditLock.unlock(userId);
                    log.error("사용 가능한 크레딧을 찾을 수 없습니다. user: {}", userId);
                    throw new CreditException(ErrorCode.CREDIT_NOT_FOUND);
                });

        credit.reserve();
        creditRepository.save(credit);
        // 락은 이미지 생성이 끝날 때까지 유지되며, 호출자가 releaseLock 으로 해제한다.
        return new CreditReservation(credit.getId(), userId);
    }

    @Override
    @Transactional
    public void commit(CreditReservation reservation) {
        creditRepository.deleteById(reservation.creditId());
    }

    @Override
    @Transactional
    public void rollback(CreditReservation reservation) {
        // 이미 삭제(커밋)되었을 수 있으니 다시 조회. PENDING 일 때만 ACTIVE 로 복구(멱등).
        creditRepository.findById(reservation.creditId()).ifPresent(credit -> {
            credit.restore();
            creditRepository.save(credit);
        });
    }

    @Override
    public void releaseLock(Long userId) {
        creditLock.unlock(userId);
    }

    @Override
    public long countActive(Long userId) {
        return creditRepository.countByUserIdAndStatus(userId, CreditStatus.ACTIVE);
    }

    @Override
    @Transactional
    public void deleteAll(Long userId) {
        creditRepository.deleteAllByUserId(userId);
    }

    private void acquireLockOrThrow(Long userId) {
        if (!creditLock.tryLock(userId)) {
            log.error("크레딧 락 획득 실패, user: {}", userId);
            throw new CreditException(ErrorCode.CREDIT_LOCK_FAILED);
        }
    }
}
