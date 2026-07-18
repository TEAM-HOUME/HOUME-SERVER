package or.sopt.houme.tag.infra.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.domain.house.model.entity.QHouse;
import or.sopt.houme.domain.house.model.entity.mapping.QHouseTaste;
import or.sopt.houme.taste.infra.persistence.QTasteJpaEntity;
import or.sopt.houme.tastetag.infra.persistence.QTasteTagJpaEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 태그 클러스터 조인 조회 (QueryDSL). 기존 {@code TagRepositoryImpl} 로직을 infra 로 이관.
 * 반환 타입은 영속 엔티티({@link TagJpaEntity})이며, 도메인 매핑은 어댑터가 수행한다.
 */
@Repository
@RequiredArgsConstructor
public class TagQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Optional<TagJpaEntity> findTagByUserIdAndImageId(Long userId, Long imageId) {
        QHouse house = QHouse.house;
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QTagJpaEntity tag = QTagJpaEntity.tagJpaEntity;
        QTasteTagJpaEntity tasteTag = QTasteTagJpaEntity.tasteTagJpaEntity;
        QTasteJpaEntity taste = QTasteJpaEntity.tasteJpaEntity;
        QHouseTaste houseTaste = QHouseTaste.houseTaste;

        return Optional.ofNullable(
                queryFactory
                        .select(tag)
                        .from(tag)
                        .join(tasteTag).on(tasteTag.tagId.eq(tag.id))
                        .join(taste).on(tasteTag.tasteId.eq(taste.id))
                        .join(houseTaste).on(houseTaste.tasteId.eq(taste.id))
                        .join(houseTaste.house, house)
                        .join(generateImage).on(generateImage.house.eq(house))
                        .where(
                                house.user.id.eq(userId),
                                generateImage.id.eq(imageId)
                        )
                        .groupBy(tag.id, tag.priority)
                        .orderBy(
                                tasteTag.count().desc(),
                                tag.priority.asc()
                        )
                        .limit(1)
                        .fetchOne()
        );
    }

    public Optional<TagJpaEntity> findMostFrequentTagByHouseId(Long houseId) {
        QTagJpaEntity tag = QTagJpaEntity.tagJpaEntity;
        QTasteTagJpaEntity tasteTag = QTasteTagJpaEntity.tasteTagJpaEntity;
        QTasteJpaEntity taste = QTasteJpaEntity.tasteJpaEntity;
        QHouseTaste houseTaste = QHouseTaste.houseTaste;

        return Optional.ofNullable(queryFactory
                .select(tag)
                .from(tag)
                .join(tasteTag).on(tasteTag.tagId.eq(tag.id))
                .join(taste).on(tasteTag.tasteId.eq(taste.id))
                .join(houseTaste).on(houseTaste.tasteId.eq(taste.id))
                .where(houseTaste.house.id.eq(houseId))
                .groupBy(tag.id, tag.priority)
                .orderBy(
                        tasteTag.count().desc(),
                        tag.priority.asc()
                )
                .limit(1)
                .fetchOne());
    }

    public Optional<TagJpaEntity> findTagByTasteId(Long tasteId) {
        QTasteJpaEntity taste = QTasteJpaEntity.tasteJpaEntity;
        QTagJpaEntity tag = QTagJpaEntity.tagJpaEntity;
        QTasteTagJpaEntity tasteTag = QTasteTagJpaEntity.tasteTagJpaEntity;

        return Optional.ofNullable(queryFactory
                .select(tag)
                .from(tasteTag)
                .join(tag).on(tasteTag.tagId.eq(tag.id))
                .join(taste).on(tasteTag.tasteId.eq(taste.id))
                .where(
                        taste.id.eq(tasteId)
                )
                .fetchOne()
        );
    }
}
