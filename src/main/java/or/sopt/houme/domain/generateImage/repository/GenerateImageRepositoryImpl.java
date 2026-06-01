package or.sopt.houme.domain.generateImage.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.banner.model.entity.QBannerCurationRawProduct;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImageType;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImage;
import or.sopt.houme.domain.generateImage.model.entity.QGenerateImageRawProduct;
import or.sopt.houme.domain.banner.model.entity.QBanner;
import or.sopt.houme.domain.house.model.entity.QHouse;
import or.sopt.houme.domain.user.model.entity.QUser;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenerateImageRepositoryImpl implements GenerateImageRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<GenerateImage> findByIdWithHouseAndUser(Long imageId) {
        QGenerateImage generateImage = QGenerateImage.generateImage;
        QHouse house = QHouse.house;
        QUser user = QUser.user;

        return Optional.ofNullable(queryFactory
                .selectFrom(generateImage)
                .join(generateImage.house, house).fetchJoin()
                .join(house.user, user).fetchJoin()
                .where(generateImage.id.eq(imageId))
                .fetchOne());
    }

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
                .leftJoin(house.banner, banner).fetchJoin()
                .where(
                        house.user.id.eq(userId),
                        house.isValid.isTrue()
                )
                .orderBy(generateImage.createdAt.desc(), generateImage.id.desc())
                .fetch();
    }

    @Override
    public List<GenerateImage> findRelatedImagesByRawProductIds(
            List<Long> rawProductIds,
            Long excludeImageId,
            int limit,
            Set<GenerateImageType> generationTypes
    ) {
        if (rawProductIds == null || rawProductIds.isEmpty() || limit < 1) {
            return List.of();
        }

        QGenerateImage generateImage = QGenerateImage.generateImage;
        QBannerCurationRawProduct bannerRawMapping = QBannerCurationRawProduct.bannerCurationRawProduct;
        QGenerateImageRawProduct rawProductMapping = QGenerateImageRawProduct.generateImageRawProduct;

        List<Long> imageIds = queryFactory
                .select(generateImage.id)
                .from(generateImage)
                .where(
                        generateImage.id.ne(excludeImageId),
                        resolvedGenerationTypeCondition(generateImage, generationTypes),
                        hasBannerMappedRawProducts(generateImage, bannerRawMapping, rawProductIds)
                                .or(hasRawProductMappings(generateImage, rawProductMapping, rawProductIds))
                )
                .orderBy(generateImage.createdAt.desc(), generateImage.id.desc())
                .limit(limit)
                .fetch();

        if (imageIds.isEmpty()) {
            return List.of();
        }

        List<GenerateImage> images = queryFactory
                .selectFrom(generateImage)
                .where(generateImage.id.in(imageIds))
                .fetch();

        Map<Long, GenerateImage> byId = images.stream()
                .collect(Collectors.toMap(GenerateImage::getId, Function.identity(), (left, right) -> left));

        return imageIds.stream()
                .map(byId::get)
                .filter(image -> image != null)
                .toList();
    }

    private BooleanExpression resolvedGenerationTypeCondition(
            QGenerateImage generateImage,
            Set<GenerateImageType> generationTypes
    ) {
        if (generationTypes == null) {
            return null;
        }
        if (generationTypes.isEmpty()) {
            // empty set means "allow no generation type"
            return generateImage.id.isNull();
        }
        return generateImage.generationType.in(generationTypes);
    }

    private BooleanExpression hasBannerMappedRawProducts(
            QGenerateImage generateImage,
            QBannerCurationRawProduct bannerRawMapping,
            List<Long> rawProductIds
    ) {
        BooleanExpression mappedByHouseBanner = JPAExpressions.selectOne()
                .from(bannerRawMapping)
                .where(
                        bannerRawMapping.banner.id.eq(generateImage.house.banner.id),
                        bannerRawMapping.curationRawProduct.id.in(rawProductIds)
                )
                .exists();

        return mappedByHouseBanner;
    }

    private BooleanExpression hasRawProductMappings(
            QGenerateImage generateImage,
            QGenerateImageRawProduct rawProductMapping,
            List<Long> rawProductIds
    ) {
        return JPAExpressions.selectOne()
                .from(rawProductMapping)
                .where(
                        rawProductMapping.generateImage.id.eq(generateImage.id),
                        rawProductMapping.curationRawProduct.id.in(rawProductIds)
                )
                .exists();
    }
}
