package or.sopt.houme.domain.generateImage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.domain.house.model.entity.QHouse;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenerateImageRepositoryImpl implements GenerateImageRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<GenerateImage> findByHouseId(Long houseId) {
        QGenerateImage generateImage = QGenerateImage.generateImage;

        return Optional.ofNullable(queryFactory
                .selectFrom(generateImage)
                .where(generateImage.house.id.eq(houseId))
                .orderBy(generateImage.createdAt.asc())
                .fetchFirst());
    }

    // 가장 최근 생성된 GenerateImage 1개 가져오기 (생성시간 내림차순, Id 내림차순)
    @Override
    public Optional<GenerateImage> findLastGenerateImage(Long houseId) {
        QGenerateImage generateImage = QGenerateImage.generateImage;

        return Optional.ofNullable(queryFactory
                .selectFrom(generateImage)
                .where(generateImage.house.id.eq(houseId))
                .orderBy(
                        generateImage.createdAt.desc(),
                        generateImage.id.desc())
                .limit(1)
                .fetchFirst());
    }

    @Override
    public Optional<GenerateImage> findMostRecentByUserId(Long userId) {
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QHouse house = QHouse.house;

        return Optional.ofNullable(queryFactory
                .selectFrom(generateImage)
                .join(generateImage.house, house).fetchJoin()
                .where(house.user.id.eq(userId))
                .orderBy(
                        generateImage.createdAt.desc(),
                        generateImage.id.desc())
                .limit(1)
                .fetchFirst());
    }

    @Override
    public List<GenerateImage> findGenerateImagesByHouseId(Long houseId) {
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QHouse house = QHouse.house;

        return queryFactory
                .selectFrom(generateImage)
                .join(generateImage.house, house).fetchJoin()
                .where(house.id.eq(houseId))
                .orderBy(generateImage.createdAt.asc())
                .fetch();
    }
}
