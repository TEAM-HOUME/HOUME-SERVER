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
                        tag.priority.asc()
                )
                .limit(1)
                .fetchOne());
    }

    // 태그 상위 2개 반환 ( 가장 많은 갯수 -> 동률일시 우선순위 )
    @Override
    public List<Tag> findBestTasteIdList(List<Long> tasteIds) {
        QTasteTag tasteTag = QTasteTag.tasteTag;
        QTag tag = QTag.tag;

        return queryFactory
                .select(tasteTag.tag)
                .from(tasteTag)
                .join(tasteTag.tag, tag)
                .where(tasteTag.taste.id.in(tasteIds))
                // Tag 기준 그룹화
                .groupBy(tasteTag.tag)
                .orderBy(
                        tasteTag.tag.count().desc(), // 1순위: 선택된 횟수가 많은 순서
                        tag.priority.asc()  // 2순위: 횟수가 같다면 우선순위가 높은 순서 (숫자가 낮은)
                )
                // 상위 2개 반환
                .limit(2)
                .fetch();
    }
}
