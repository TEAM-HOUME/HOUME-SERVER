package or.sopt.houme.compare.infra.repository;

import or.sopt.houme.compare.infra.entity.CompareHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompareHistoryRepository extends JpaRepository<CompareHistoryJpaEntity, Long> {
}
