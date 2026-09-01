package or.sopt.houme.domain.coupang.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.coupang.model.entity.QCoupangKeywordProductJpaEntity;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CoupangKeywordProductJpaRepositoryImpl implements CoupangKeywordProductJpaRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public void deleteByKeywordId(Long keywordId) {
        QCoupangKeywordProductJpaEntity keywordProduct = QCoupangKeywordProductJpaEntity.coupangKeywordProductJpaEntity;
        queryFactory
                .delete(keywordProduct)
                .where(keywordProduct.keyword.id.eq(keywordId))
                .execute();
    }
}
