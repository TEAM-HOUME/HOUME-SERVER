package or.sopt.houme.domain.generateImage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.QBannerCurationRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImageUsedProduct;
import or.sopt.houme.domain.banner.model.entity.QBanner;
import or.sopt.houme.domain.house.model.entity.QHouse;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<GenerateImage> findAllByUserIdWithHouseAndBanner(Long userId) {
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QHouse house = QHouse.house;
        QBanner banner = QBanner.banner;

        return queryFactory
                .selectFrom(generateImage)
                .join(generateImage.house, house).fetchJoin()
                .leftJoin(generateImage.banner, banner).fetchJoin()
                .where(
                        house.user.id.eq(userId),
                        house.isValid.isTrue()
                )
                .orderBy(generateImage.createdAt.desc(), generateImage.id.desc())
                .fetch();
    }

    @Override
    public List<GenerateImage> findRelatedImagesByRawProductIds(List<Long> rawProductIds, Long excludeImageId, int limit) {
        if (rawProductIds == null || rawProductIds.isEmpty() || limit < 1) {
            return List.of();
        }

        QGenerateImage generateImage = QGenerateImage.generateImage;
        QBanner banner = QBanner.banner;
        QBannerCurationRawProduct bannerRawMapping = QBannerCurationRawProduct.bannerCurationRawProduct;
        QGenerateImageUsedProduct usedProductMapping = QGenerateImageUsedProduct.generateImageUsedProduct;

        List<GenerateImage> rows = queryFactory
                .selectFrom(generateImage)
                .leftJoin(generateImage.banner, banner).fetchJoin()
                .leftJoin(banner.bannerRawProducts, bannerRawMapping)
                .leftJoin(usedProductMapping).on(usedProductMapping.generateImage.id.eq(generateImage.id))
                .where(
                        generateImage.id.ne(excludeImageId),
                        bannerRawMapping.curationRawProduct.id.in(rawProductIds)
                                .or(usedProductMapping.curationRawProduct.id.in(rawProductIds))
                )
                .orderBy(generateImage.createdAt.desc(), generateImage.id.desc())
                .fetch();

        Map<Long, GenerateImage> distinctById = new LinkedHashMap<>();
        for (GenerateImage row : rows) {
            if (row == null || row.getId() == null) {
                continue;
            }
            distinctById.putIfAbsent(row.getId(), row);
            if (distinctById.size() >= limit) {
                break;
            }
        }
        return List.copyOf(distinctById.values());
    }
}
