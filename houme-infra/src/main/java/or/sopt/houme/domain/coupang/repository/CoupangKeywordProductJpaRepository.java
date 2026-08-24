package or.sopt.houme.domain.coupang.repository;

import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupangKeywordProductJpaRepository extends JpaRepository<CoupangKeywordProductJpaEntity, Long> {
    void deleteByKeywordId(Long keywordId);
}
