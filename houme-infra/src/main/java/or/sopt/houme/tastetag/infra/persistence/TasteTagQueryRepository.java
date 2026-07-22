package or.sopt.houme.tastetag.infra.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.tag.infra.persistence.QTagJpaEntity;
import or.sopt.houme.tag.infra.persistence.TagJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 무드보드-태그 매핑 조인 조회 (QueryDSL). 기존 {@code TasteTagCustomRepositoryImpl} 로직을 이관하되,
 * TasteTag→Tag 연관 절단(tagId 참조)에 맞춰 명시적 id 조인으로 재작성했다.
 * 반환 타입은 {@link TagJpaEntity} 이며, 도메인 매핑은 어댑터가 수행한다.
 */
@Repository
@RequiredArgsConstructor
public class TasteTagQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 받은 무드보드(tasteIds) 중 가장 우선순위가 높은 Tag 반환
    public Optional<TagJpaEntity> findBestTasteId(List<Long> tasteIds) {
        QTasteTagJpaEntity tasteTag = QTasteTagJpaEntity.tasteTagJpaEntity;
        QTagJpaEntity tag = QTagJpaEntity.tagJpaEntity;

        return Optional.ofNullable(queryFactory
                .select(tag)
                .from(tasteTag)
                .join(tag).on(tasteTag.tagId.eq(tag.id))
                .where(tasteTag.tasteId.in(tasteIds))
                .groupBy(
                        tasteTag.tasteId,
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

    // 태그 상위 2개 반환 (가장 많은 갯수 -> 동률일시 우선순위)
    public List<TagJpaEntity> findBestTasteIdList(List<Long> tasteIds) {
        QTasteTagJpaEntity tasteTag = QTasteTagJpaEntity.tasteTagJpaEntity;
        QTagJpaEntity tag = QTagJpaEntity.tagJpaEntity;

        return queryFactory
                .select(tag)
                .from(tasteTag)
                .join(tag).on(tasteTag.tagId.eq(tag.id))
                .where(tasteTag.tasteId.in(tasteIds))
                .groupBy(tag)
                .orderBy(
                        tag.count().desc(),   // 1순위: 선택된 횟수가 많은 순서
                        tag.priority.asc()    // 2순위: 우선순위가 높은(숫자가 낮은) 순서
                )
                .limit(2)
                .fetch();
    }

    // tasteIds에 연결된 모든 Tag를 우선순위 오름차순으로 중복 제거하여 반환
    public List<TagJpaEntity> findDistinctTagsByTasteIdIn(List<Long> tasteIds) {
        QTasteTagJpaEntity tasteTag = QTasteTagJpaEntity.tasteTagJpaEntity;
        QTagJpaEntity tag = QTagJpaEntity.tagJpaEntity;

        return queryFactory
                .select(tag)
                .distinct()
                .from(tasteTag)
                .join(tag).on(tasteTag.tagId.eq(tag.id))
                .where(tasteTag.tasteId.in(tasteIds))
                .orderBy(tag.priority.asc())
                .fetch();
    }
}
