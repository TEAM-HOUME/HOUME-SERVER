package or.sopt.houme.compare.infra.repository;

import or.sopt.houme.compare.infra.entity.CompareHistoryJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompareHistoryRepository extends JpaRepository<CompareHistoryJpaEntity, Long> {

    List<CompareHistoryJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query(value = """
            SELECT * FROM compare_history
            WHERE user_id = :userId
              AND id IN (
                  SELECT MAX(id) FROM compare_history
                  WHERE user_id = :userId
                  GROUP BY source_url
              )
            ORDER BY created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<CompareHistoryJpaEntity> findDistinctBySourceUrlOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );
}
