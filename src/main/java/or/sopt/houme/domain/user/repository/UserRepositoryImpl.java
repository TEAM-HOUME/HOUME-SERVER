package or.sopt.houme.domain.user.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.entity.CreditStatus;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.house.entity.QHouse;
import or.sopt.houme.domain.house.entity.mapping.QHouseTaste;
import or.sopt.houme.domain.taste.entity.QTag;
import or.sopt.houme.domain.taste.entity.QTaste;
import or.sopt.houme.domain.taste.entity.QTasteTag;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryDTO;
import or.sopt.houme.domain.user.entity.QUser;
import or.sopt.houme.domain.generateImage.entity.QGenerateImage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static or.sopt.houme.domain.credit.entity.QCredit.credit;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Long countByMemberIdAndStatus(Long userId) {
        return queryFactory
                .select(credit.count())
                .from(credit)
                .where(
                        credit.user.id.eq(userId),
                        credit.status.eq(CreditStatus.ACTIVE)
                )
                .fetchOne();
    }

    @Override
    public List<UserImageHistoryDTO> getUserImageHistory(Long userId) {
        QUser user = QUser.user;
        QHouse house = QHouse.house;
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QHouseTaste houseTaste = QHouseTaste.houseTaste;
        QTaste taste = QTaste.taste;
        QTasteTag tasteTag = QTasteTag.tasteTag;
        QTag tag = QTag.tag;

        return queryFactory
                .select(Projections.constructor(
                        UserImageHistoryDTO.class,
                        generateImage.url,
                        tag.tagName,
                        house.equilibrium,
                        house.form
                ))
                .from(user)
                .join(user.houses, house)
                .join(house.generateImage, generateImage)
                .join(houseTaste).on(houseTaste.house.eq(house))
                .join(houseTaste.taste, taste)
                .join(tasteTag).on(tasteTag.taste.eq(taste))
                .join(tasteTag.tag, tag)
                .where(user.id.eq(userId))
                .fetch();
    }

    @Override
    public Optional<GenerateImage> findImageHistoryById(Long userId) {
        QUser user = QUser.user;
        QHouse house = QHouse.house;
        QGenerateImage generateImage = QGenerateImage.generateImage;

        return Optional.ofNullable(queryFactory
                .select(generateImage)
                .from(house)
                .join(house.generateImage, generateImage)
                .join(house.user, user)
                .where(user.id.eq(userId))
                .limit(1)
                .fetchOne());
    }
}
