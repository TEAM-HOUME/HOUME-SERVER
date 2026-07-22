package or.sopt.houme.taste.infra.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 취향(무드보드) cursor 페이지네이션 조회 (QueryDSL). 기존 {@code TasteCustomRepositoryImpl} 로직 이관.
 */
@Repository
@RequiredArgsConstructor
public class TasteQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<TasteJpaEntity> findTasteByCursor(Long cursorId, int size) {
        QTasteJpaEntity taste = QTasteJpaEntity.tasteJpaEntity;

        return queryFactory
                .selectFrom(taste)
                .where(ltCursorId(cursorId))
                .orderBy(taste.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId != null ? QTasteJpaEntity.tasteJpaEntity.id.lt(cursorId) : null;
    }
}
