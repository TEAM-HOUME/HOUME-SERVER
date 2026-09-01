package or.sopt.houme.domain.coupang.repository;

import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupangProductJpaRepository extends JpaRepository<CoupangProductJpaEntity, Long> {
    Optional<CoupangProductJpaEntity> findByCoupangProductId(String coupangProductId);
}
