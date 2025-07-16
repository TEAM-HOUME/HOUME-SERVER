package or.sopt.houme.domain.taste.repository.taste_tag;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.taste.entity.QTag;
import or.sopt.houme.domain.taste.entity.QTasteTag;
import or.sopt.houme.domain.taste.entity.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TasteTagCustomRepositoryImpl implements TasteTagCustomRepository {

    private final JPAQueryFactory queryFactory;

    // 받은 무드보드(tasteIds) 중 가장 우선순위가 높은 Tag 반환
    @Override
    public Optional<Tag> findBestTasteId(List<Long> tasteIds) {
        QTasteTag tasteTag = QTasteTag.tasteTag;
        QTag tag = QTag.tag;

        return Optional.ofNullable(queryFactory
                .select(tasteTag.tag)
                .from(tasteTag)
                .join(tasteTag.tag, tag)
                .where(tasteTag.taste.id.in(tasteIds))
                .groupBy(
                        tasteTag.taste.id,
                        tag.id,
                        tag.priority,
                        tag.tagName,
                        tag.tagNameKr,
                        tag.tagPrompt
                )
                .orderBy(
                        tasteTag.count().desc(),
                        tag.priority.max().desc()
                )
                .limit(1)
                .fetchOne());
    }
}
