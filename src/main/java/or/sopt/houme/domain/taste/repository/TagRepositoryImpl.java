package or.sopt.houme.domain.taste.repository;

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
                        .selectFrom(tag)
                        .join(tasteTag).on(tasteTag.tag.eq(tag))
                        .join(tasteTag.taste, taste)
                        .join(houseTaste).on(houseTaste.taste.eq(taste))
                        .join(houseTaste.house, house)
                        .join(generateImage).on(generateImage.house.eq(house))
                        .where(
                                house.user.id.eq(userId),
                                generateImage.id.eq(imageId)
                        )
                        .fetchOne()
        );
    }
}
