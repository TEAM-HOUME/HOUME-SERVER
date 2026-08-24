package or.sopt.houme.domain.coupang.repository;

import jakarta.persistence.LockModeType;
import or.sopt.houme.domain.coupang.model.entity.CoupangApiCallControlJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface CoupangApiCallControlJpaRepository extends JpaRepository<CoupangApiCallControlJpaEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CoupangApiCallControlJpaEntity> findWithLockById(Long id);
}
