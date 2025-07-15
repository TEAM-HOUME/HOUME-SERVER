package or.sopt.houme.domain.taste.repository.taste;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.taste.entity.Taste;
import org.springframework.stereotype.Repository;
import or.sopt.houme.domain.taste.entity.QTaste;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TasteCustomRepositoryImpl implements TasteCustomRepository{

    private final JPAQueryFactory queryFactory;

    // 커서기반 페이지네이션
    @Override
    public List<Taste> findTasteByCursor(Long cursorId, int size) {

        QTaste qTaste = QTaste.taste;

        return queryFactory
                .selectFrom(qTaste)
                .where(ltCursorId(cursorId))
                .orderBy(qTaste.id.desc())
                .limit(size)
                .fetch();

        // 아래는 오래된 순서부터
//                .where(gtCursorId(cursorId)) // 커서 기준 ID보다 큰 데이터 = 더 최신
//                .orderBy(qTaste.id.asc())    // 오래된 순
    }

    // 조건 검사
    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId != null ? QTaste.taste.id.lt(cursorId) : null;
    }
}
