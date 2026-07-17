package or.sopt.houme.credit.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.credit.domain.Credit;
import or.sopt.houme.credit.domain.CreditStatus;
import or.sopt.houme.credit.domain.port.out.CreditRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link CreditRepositoryPort} 의 JPA/QueryDSL 구현 어댑터.
 */
@Component
@RequiredArgsConstructor
public class CreditPersistenceAdapter implements CreditRepositoryPort {

    private final CreditJpaRepository jpaRepository;
    private final CreditQueryRepository queryRepository;

    @Override
    public void saveAll(List<Credit> credits) {
        jpaRepository.saveAll(credits.stream().map(CreditMapper::toNewEntity).toList());
    }

    @Override
    public Optional<Credit> findOldestByUserIdAndStatus(Long userId, CreditStatus status) {
        return queryRepository.findOldestByUserIdAndStatus(userId, status).map(CreditMapper::toDomain);
    }

    @Override
    public Optional<Credit> findById(Long creditId) {
        return jpaRepository.findById(creditId).map(CreditMapper::toDomain);
    }

    @Override
    public void save(Credit credit) {
        // 상태 변경(PENDING 예약/복구) 반영: 기존 레코드를 조회해 상태만 갱신한다.
        jpaRepository.findById(credit.getId())
                .ifPresent(entity -> {
                    entity.updateStatus(credit.getStatus());
                    jpaRepository.save(entity);
                });
    }

    @Override
    public void deleteById(Long creditId) {
        jpaRepository.deleteById(creditId);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public long countByUserIdAndStatus(Long userId, CreditStatus status) {
        return jpaRepository.countByUserIdAndStatus(userId, status);
    }
}
