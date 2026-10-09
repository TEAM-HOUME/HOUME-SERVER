package or.sopt.houme.compare.infra.repository;

import or.sopt.houme.compare.infra.entity.CompareHistoryJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompareHistoryRepository
        extends JpaRepository<CompareHistoryJpaEntity, Long>, CompareHistoryRepositoryCustom {

    List<CompareHistoryJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<CompareHistoryJpaEntity> findFirstByUserIdAndSourceUrlOrderByIdDesc(Long userId, String sourceUrl);
}
