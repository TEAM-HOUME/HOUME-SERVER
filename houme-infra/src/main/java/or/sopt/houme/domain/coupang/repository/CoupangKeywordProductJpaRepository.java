package or.sopt.houme.domain.coupang.repository;

import or.sopt.houme.domain.coupang.model.entity.CoupangKeywordProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoupangKeywordProductJpaRepository extends JpaRepository<CoupangKeywordProductJpaEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from CoupangKeywordProductJpaEntity mapping where mapping.keyword.id = :keywordId")
    void deleteByKeywordId(@Param("keywordId") Long keywordId);
}
