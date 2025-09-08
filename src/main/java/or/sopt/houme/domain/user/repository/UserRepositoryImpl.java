package or.sopt.houme.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.credit.entity.CreditStatus;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.entity.QGenerateImage;
import or.sopt.houme.domain.house.entity.QHouse;
import or.sopt.houme.domain.user.entity.QUser;
import org.springframework.stereotype.Repository;

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
    public Optional<GenerateImage> findImageHistoryById(Long userId) {
        QUser user = QUser.user;
        QHouse house = QHouse.house;
        QGenerateImage generateImage = QGenerateImage.generateImage;

        return Optional.ofNullable(queryFactory
                .select(generateImage)
                .from(house)
                .join(house.generateImages, generateImage)
                .join(house.user, user)
                .where(user.id.eq(userId))
                .limit(1)
                .fetchOne());
    }
}
