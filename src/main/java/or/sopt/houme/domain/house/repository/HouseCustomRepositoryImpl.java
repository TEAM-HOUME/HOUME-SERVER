package or.sopt.houme.domain.house.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.QGenerateImage;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.QHouse;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HouseCustomRepositoryImpl implements HouseCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public House findLatestHouse(User user) {
        QHouse qHouse = QHouse.house;

        return queryFactory
                .selectFrom(qHouse)
                .where(qHouse.user.eq(user))
                .orderBy(qHouse.id.desc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public Optional<House> findHouseByUserIdAndImageId(Long userId, Long imageId) {
        QHouse house = QHouse.house;
        QGenerateImage generateImage = QGenerateImage.generateImage;

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(house)
                        .join(generateImage)
                        .on(generateImage.house.eq(house))
                        .where(
                                house.user.id.eq(userId),
                                generateImage.id.eq(imageId)
                        )
                        .fetchOne()
        );
    }
}
