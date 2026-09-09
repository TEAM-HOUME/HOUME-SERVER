package or.sopt.houme.compare.infra.repository;

import or.sopt.houme.compare.infra.entity.CompareHistoryJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompareHistoryRepository extends JpaRepository<CompareHistoryJpaEntity, Long> {

    List<CompareHistoryJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
