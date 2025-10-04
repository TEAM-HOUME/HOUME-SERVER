package or.sopt.houme.domain.taste.repository.tag;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.QGenerateImage;
import or.sopt.houme.domain.house.entity.QHouse;
import or.sopt.houme.domain.house.entity.mapping.QHouseTaste;
import or.sopt.houme.domain.taste.entity.QTag;
import or.sopt.houme.domain.taste.entity.QTaste;
import or.sopt.houme.domain.taste.entity.QTasteTag;
import or.sopt.houme.domain.taste.entity.Tag;
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
}
