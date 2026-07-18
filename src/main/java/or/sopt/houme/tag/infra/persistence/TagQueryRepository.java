package or.sopt.houme.tag.infra.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.domain.house.model.entity.QHouse;
import or.sopt.houme.domain.house.model.entity.mapping.QHouseTaste;
import or.sopt.houme.domain.house.model.taste.entity.QTasteTag;
import or.sopt.houme.taste.infra.persistence.QTasteJpaEntity;
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
        QTasteTag tasteTag = QTasteTag.tasteTag;
        QTasteJpaEntity taste = QTasteJpaEntity.tasteJpaEntity;
        QHouseTaste houseTaste = QHouseTaste.houseTaste;

        return Optional.ofNullable(
                queryFactory
                        .select(tag)
                        .from(tag)
                        .join(tasteTag).on(tasteTag.tag.eq(tag))
                        .join(taste).on(tasteTag.taste.eq(taste))
                        .join(houseTaste).on(houseTaste.taste.eq(taste))
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
        QTasteTag tasteTag = QTasteTag.tasteTag;
        QTasteJpaEntity taste = QTasteJpaEntity.tasteJpaEntity;
        QHouseTaste houseTaste = QHouseTaste.houseTaste;

        return Optional.ofNullable(queryFactory
                .select(tag)
                .from(tag)
                .join(tasteTag).on(tasteTag.tag.eq(tag))
                .join(taste).on(tasteTag.taste.eq(taste))
                .join(houseTaste).on(houseTaste.taste.eq(taste))
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
        QTasteTag tasteTag = QTasteTag.tasteTag;

        return Optional.ofNullable(queryFactory
                .select(tag)
                .from(tasteTag)
                .join(tasteTag.tag, tag)
                .join(tasteTag.taste, taste)
                .where(
                        taste.id.eq(tasteId)
                )
                .fetchOne()
        );
    }
}
