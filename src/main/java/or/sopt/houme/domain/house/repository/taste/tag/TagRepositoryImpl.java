package or.sopt.houme.domain.house.repository.taste.tag;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.domain.house.model.entity.QHouse;
import or.sopt.houme.domain.house.model.entity.mapping.QHouseTaste;
import or.sopt.houme.domain.house.model.taste.entity.QTag;
import or.sopt.houme.domain.house.model.taste.entity.QTaste;
import or.sopt.houme.domain.house.model.taste.entity.QTasteTag;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Tag> findTagByUserIdAndImageId(Long userId, Long imageId) {
        QHouse house = QHouse.house;
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QTag tag = QTag.tag;
        QTasteTag tasteTag = QTasteTag.tasteTag;
        QTaste taste = QTaste.taste;
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

    @Override
    public Optional<Tag> findMostFrequentTagByHouseId(Long houseId) {
        QTag tag = QTag.tag;
        QTasteTag tasteTag = QTasteTag.tasteTag;
        QTaste taste = QTaste.taste;
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

    // tasteId로 Tag 반환
    @Override
    public Optional<Tag> findTagByTasteId(Long tasteId) {
        QTaste taste = QTaste.taste;
        QTag tag = QTag.tag;
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
