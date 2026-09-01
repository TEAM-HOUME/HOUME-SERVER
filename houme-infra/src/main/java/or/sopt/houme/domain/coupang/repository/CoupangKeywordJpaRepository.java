package or.sopt.houme.domain.coupang.repository;

import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupangKeywordJpaRepository extends JpaRepository<CoupangKeywordJpaEntity, Long> {

    Optional<CoupangKeywordJpaEntity> findByKeyword(String keyword);
}
