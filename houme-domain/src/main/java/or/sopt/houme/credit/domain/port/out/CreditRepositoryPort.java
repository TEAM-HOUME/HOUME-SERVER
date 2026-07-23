package or.sopt.houme.credit.domain.port.out;

import or.sopt.houme.credit.domain.Credit;
import or.sopt.houme.credit.domain.CreditStatus;

import java.util.List;
import java.util.Optional;

/**
 * 크레딧 영속화 아웃바운드 포트. 도메인/애플리케이션은 이 인터페이스만 알고,
 * 구현(JPA·QueryDSL)은 infra 어댑터가 제공한다.
 */
public interface CreditRepositoryPort {

    /** 신규 크레딧 count 개를 발급·저장. */
    void saveAll(List<Credit> credits);

    /** 상태별로 가장 오래된(FIFO 소진 대상) 크레딧 1건. */
    Optional<Credit> findOldestByUserIdAndStatus(Long userId, CreditStatus status);

    Optional<Credit> findById(Long creditId);

    /** 상태 변경 후 저장 (PENDING 예약/복구 반영). */
    void save(Credit credit);

    void deleteById(Long creditId);

    void deleteAllByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, CreditStatus status);
}
