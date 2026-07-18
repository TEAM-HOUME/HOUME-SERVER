package or.sopt.houme.domain.house.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.house.infra.persistence.HouseJpaEntity;
import or.sopt.houme.house.infra.persistence.QHouseJpaEntity;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HouseCustomRepositoryImpl implements HouseCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public HouseJpaEntity findLatestHouse(User user) {
        QHouseJpaEntity qHouse = QHouseJpaEntity.houseJpaEntity;

        return queryFactory
                .selectFrom(qHouse)
                .where(qHouse.user.eq(user))
                .orderBy(qHouse.id.desc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public Optional<HouseJpaEntity> findHouseByUserIdAndImageId(Long userId, Long imageId) {
        QHouseJpaEntity house = QHouseJpaEntity.houseJpaEntity;
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

    @Override
    public List<HouseJpaEntity> findValidHouseByUserId(Long userId) {
        QHouseJpaEntity house = QHouseJpaEntity.houseJpaEntity;

        return queryFactory
                .selectFrom(house)
                .where(
                        house.user.id.eq(userId)
                )
                .orderBy(house.id.desc())
                .fetch();
    }
}
