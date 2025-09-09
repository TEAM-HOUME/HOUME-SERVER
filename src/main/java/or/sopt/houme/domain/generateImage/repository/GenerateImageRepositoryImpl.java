package or.sopt.houme.domain.generateImage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.entity.QGenerateImage;
import or.sopt.houme.domain.house.entity.QHouse;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenerateImageRepositoryImpl implements GenerateImageRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<GenerateImage> findGenerateImageByUserIdAndImageId(Long userId, Long imageId) {
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QHouse house = QHouse.house;

        return Optional.ofNullable(
                queryFactory
                        .selectFrom(generateImage)
                        .join(house).on(generateImage.house.eq(house))
                        .where(
                                house.user.id.eq(userId),
                                generateImage.id.eq(imageId)
                        )
                        .fetchOne()
        );
    }
    @Override
    public Optional<GenerateImage> findByHouseId(Long houseId) {
        QGenerateImage generateImage = QGenerateImage.generateImage;

        return Optional.ofNullable(queryFactory
                .selectFrom(generateImage)
                .where(generateImage.house.id.eq(houseId))
                .fetchOne());
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
                        generateImage.id.asc())
                .fetchOne());
    }
}
