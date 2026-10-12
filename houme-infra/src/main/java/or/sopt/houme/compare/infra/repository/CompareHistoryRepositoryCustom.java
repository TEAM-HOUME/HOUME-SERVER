package or.sopt.houme.compare.infra.repository;

import or.sopt.houme.compare.infra.entity.CompareHistoryJpaEntity;

import java.util.List;

public interface CompareHistoryRepositoryCustom {

    List<CompareHistoryJpaEntity> findDistinctBySourceUrlOrderByCreatedAtDesc(Long userId, int limit);
}
