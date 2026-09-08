package or.sopt.houme.domain.coupang.repository;

import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordProductJpaEntity;
import or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoupangKeywordProductJpaRepository extends JpaRepository<CoupangKeywordProductJpaEntity, Long>,
        CoupangKeywordProductJpaRepositoryCustom {

    @Query("SELECT kp.product FROM CoupangKeywordProductJpaEntity kp WHERE kp.keyword.keyword = :keyword")
    List<CoupangProductJpaEntity> findProductsByKeyword(@Param("keyword") String keyword);
}
