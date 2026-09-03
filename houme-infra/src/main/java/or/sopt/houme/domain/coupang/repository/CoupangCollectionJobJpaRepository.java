package or.sopt.houme.domain.coupang.repository;

import jakarta.persistence.LockModeType;
import or.sopt.houme.domain.coupang.model.entity.CoupangCollectionJobJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangJobStatus;
import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CoupangCollectionJobJpaRepository extends JpaRepository<CoupangCollectionJobJpaEntity, Long> {

    boolean existsByKeyword(CoupangKeywordJpaEntity keyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CoupangCollectionJobJpaEntity> findFirstByStatusAndScheduledAtLessThanEqualOrderByPriorityDescScheduledAtAsc(
            CoupangJobStatus status,
            LocalDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<CoupangCollectionJobJpaEntity> findAllByStatusAndStartedAtBefore(
            CoupangJobStatus status,
            LocalDateTime startedAt
    );
}
