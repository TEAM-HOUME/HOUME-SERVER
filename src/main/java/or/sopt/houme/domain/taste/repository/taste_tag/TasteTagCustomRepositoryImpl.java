package or.sopt.houme.domain.taste.repository.taste_tag;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.taste.entity.QTag;
import or.sopt.houme.domain.taste.entity.QTasteTag;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TasteTagCustomRepositoryImpl implements TasteTagCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Long findBestTasteId(List<Long> ids) {
        QTasteTag tasteTag = QTasteTag.tasteTag;
        QTag tag = QTag.tag;

        return queryFactory
                .select(tasteTag.taste.id)
                .from(tasteTag)
                .join(tasteTag.tag, tag)
                .where(tasteTag.taste.id.in(ids))
                .groupBy(tasteTag.taste.id)
                .orderBy(
                        tasteTag.count().desc(),
                        tag.priority.max().desc()
                )
                .limit(1)
                .fetchOne();
    }
}
