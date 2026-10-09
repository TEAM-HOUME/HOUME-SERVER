package or.sopt.houme.compare.infra.repository;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.infra.entity.CompareHistoryJpaEntity;
import or.sopt.houme.compare.infra.entity.QCompareHistoryJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CompareHistoryRepositoryImpl implements CompareHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CompareHistoryJpaEntity> findDistinctBySourceUrlOrderByCreatedAtDesc(Long userId, int limit) {
        QCompareHistoryJpaEntity ch = QCompareHistoryJpaEntity.compareHistoryJpaEntity;
        QCompareHistoryJpaEntity sub = new QCompareHistoryJpaEntity("sub");

        return queryFactory
                .selectFrom(ch)
                .where(
                        ch.userId.eq(userId),
                        ch.id.in(
                                JPAExpressions
                                        .select(sub.id.max())
                                        .from(sub)
                                        .where(sub.userId.eq(userId))
                                        .groupBy(sub.sourceUrl)
                        )
                )
                .orderBy(ch.updatedAt.desc())
                .limit(limit)
                .fetch();
    }
}
