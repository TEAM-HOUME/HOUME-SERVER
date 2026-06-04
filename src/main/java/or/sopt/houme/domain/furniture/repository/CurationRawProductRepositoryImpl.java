package or.sopt.houme.domain.furniture.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProductFurniture;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.model.entity.QFurniture;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureType;
import or.sopt.houme.domain.furniture.model.entity.QJjym;
import or.sopt.houme.domain.furniture.model.entity.QRecommendFurniture;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepositoryCustom.PriceRangeFilter;
import or.sopt.houme.domain.house.model.taste.entity.QTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class CurationRawProductRepositoryImpl implements CurationRawProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CurationRawProduct> searchByKeyword(String keyword, Pageable pageable) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        BooleanBuilder where = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            String normalizedKeyword = keyword.trim();
            where.and(
                    containsIgnoreCase(rawProduct.productName, normalizedKeyword)
                            .or(containsIgnoreCase(rawProduct.brand, normalizedKeyword))
                            .or(containsIgnoreCase(rawProduct.source, normalizedKeyword))
                            .or(rawProduct.productId.stringValue().contains(normalizedKeyword))
            );
        }

        List<CurationRawProduct> content = queryFactory
                .selectFrom(rawProduct)
                .where(where)
                .orderBy(rawProduct.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(rawProduct.count())
                .from(rawProduct)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Slice<CurationRawProduct> findAllByCurationFilters(
            String keyword,
            List<Long> typeIds,
            List<PriceRangeFilter> priceRanges,
            List<String> colorNames,
            Long cursor,
            Pageable pageable,
            List<Long> additionalProductIds
    ) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QCurationRawProductFurnitureTag mapping = QCurationRawProductFurnitureTag.curationRawProductFurnitureTag;
        QFurnitureTag fTag = QFurnitureTag.furnitureTag;
        QFurniture furniture = QFurniture.furniture;
        QFurnitureType fType = QFurnitureType.furnitureType;
        QCurationRawProductColor color = QCurationRawProductColor.curationRawProductColor;

        BooleanBuilder finalWhere = new BooleanBuilder();

        finalWhere.and(rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull()));
        if (cursor != null) {
            finalWhere.and(rawProduct.id.lt(cursor));
        }

        if (keyword != null && !keyword.isBlank()) {
            finalWhere.and(rawProduct.productName.containsIgnoreCase(keyword)
                    .or(rawProduct.brand.containsIgnoreCase(keyword)));
        }

        if ((typeIds != null && !typeIds.isEmpty()) || (additionalProductIds != null && !additionalProductIds.isEmpty())) {
            BooleanBuilder typeOrEtc = new BooleanBuilder();
            if (typeIds != null && !typeIds.isEmpty()) {
                typeOrEtc.or(rawProduct.id.in(
                        queryFactory.select(mapping.curationRawProduct.id)
                                .from(mapping)
                                .join(mapping.furnitureTag, fTag)
                                .join(fTag.furniture, furniture)
                                .leftJoin(furniture.furnitureType, fType)
                                .where(fType.id.in(typeIds).or(furniture.id.in(typeIds)))
                ));
            }
            if (additionalProductIds != null && !additionalProductIds.isEmpty()) {
                typeOrEtc.or(rawProduct.id.in(additionalProductIds));
            }
            finalWhere.and(typeOrEtc);
        }

        if (priceRanges != null && !priceRanges.isEmpty()) {
            BooleanBuilder priceGroupBuilder = new BooleanBuilder();
            for (PriceRangeFilter range : priceRanges) {
                BooleanBuilder rangeBuilder = new BooleanBuilder();
                if (range.min() != null) rangeBuilder.and(rawProduct.discountPrice.goe(range.min()));
                if (range.max() != null) rangeBuilder.and(rawProduct.discountPrice.loe(range.max()));
                priceGroupBuilder.or(rangeBuilder);
            }
            finalWhere.and(priceGroupBuilder);
        }

        if (colorNames != null && !colorNames.isEmpty()) {
            BooleanBuilder colorGroupBuilder = new BooleanBuilder();
            for (String colorName : colorNames) {
                colorGroupBuilder.or(color.clientColorName.containsIgnoreCase(colorName));
            }
            finalWhere.and(rawProduct.id.in(
                    queryFactory.select(color.curationRawProduct.id)
                            .from(color)
                            .where(colorGroupBuilder)
            ));
        }

        List<CurationRawProduct> content = queryFactory
                .selectFrom(rawProduct)
                .where(finalWhere)
                .orderBy(rawProduct.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<CurationRawProduct> findAllByCurationFiltersV2(
            String keyword,
            List<Long> typeIds,
            List<PriceRangeFilter> priceRanges,
            List<String> colorNames,
            Long cursor,
            Pageable pageable,
            List<Long> additionalProductIds
    ) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QCurationRawProductFurnitureTag mapping = QCurationRawProductFurnitureTag.curationRawProductFurnitureTag;
        QFurnitureTag fTag = QFurnitureTag.furnitureTag;
        QFurniture furniture = QFurniture.furniture;
        QFurnitureType fType = QFurnitureType.furnitureType;
        QCurationRawProductColor color = QCurationRawProductColor.curationRawProductColor;

        BooleanBuilder finalWhere = new BooleanBuilder();

        finalWhere.and(rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull()));
        if (cursor != null) {
            finalWhere.and(rawProduct.id.lt(cursor));
        }

        // v2: search_tokens 기반 검색 (pg_trgm GIN 인덱스 활용, 토큰은 lowercase 저장이므로 contains 사용)
        if (keyword != null && !keyword.isBlank()) {
            finalWhere.and(rawProduct.searchTokens.contains(keyword.toLowerCase()));
        }

        if ((typeIds != null && !typeIds.isEmpty()) || (additionalProductIds != null && !additionalProductIds.isEmpty())) {
            BooleanBuilder typeOrEtc = new BooleanBuilder();
            if (typeIds != null && !typeIds.isEmpty()) {
                typeOrEtc.or(rawProduct.id.in(
                        queryFactory.select(mapping.curationRawProduct.id)
                                .from(mapping)
                                .join(mapping.furnitureTag, fTag)
                                .join(fTag.furniture, furniture)
                                .leftJoin(furniture.furnitureType, fType)
                                .where(fType.id.in(typeIds).or(furniture.id.in(typeIds)))
                ));
            }
            if (additionalProductIds != null && !additionalProductIds.isEmpty()) {
                typeOrEtc.or(rawProduct.id.in(additionalProductIds));
            }
            finalWhere.and(typeOrEtc);
        }

        if (priceRanges != null && !priceRanges.isEmpty()) {
            BooleanBuilder priceGroupBuilder = new BooleanBuilder();
            for (PriceRangeFilter range : priceRanges) {
                BooleanBuilder rangeBuilder = new BooleanBuilder();
                if (range.min() != null) rangeBuilder.and(rawProduct.discountPrice.goe(range.min()));
                if (range.max() != null) rangeBuilder.and(rawProduct.discountPrice.loe(range.max()));
                priceGroupBuilder.or(rangeBuilder);
            }
            finalWhere.and(priceGroupBuilder);
        }

        if (colorNames != null && !colorNames.isEmpty()) {
            BooleanBuilder colorGroupBuilder = new BooleanBuilder();
            for (String colorName : colorNames) {
                colorGroupBuilder.or(color.clientColorName.containsIgnoreCase(colorName));
            }
            finalWhere.and(rawProduct.id.in(
                    queryFactory.select(color.curationRawProduct.id)
                            .from(color)
                            .where(colorGroupBuilder)
            ));
        }

        List<CurationRawProduct> content = queryFactory
                .selectFrom(rawProduct)
                .where(finalWhere)
                .orderBy(rawProduct.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Page<CurationRawProduct> findExposedRawProductsExcludingLikedByUser(Long userId, Pageable pageable) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QJjym jjym = QJjym.jjym;
        QRecommendFurniture recommendFurniture = QRecommendFurniture.recommendFurniture;

        BooleanExpression isNotLikedByUser = JPAExpressions
                .selectOne()
                .from(jjym)
                .join(jjym.recommendFurniture, recommendFurniture)
                .where(
                        jjym.user.id.eq(userId),
                        recommendFurniture.source.eq(CurationSource.RAW),
                        recommendFurniture.furnitureProductId.eq(rawProduct.productId)
                )
                .notExists();

        List<CurationRawProduct> content = queryFactory
                .selectFrom(rawProduct)
                .where(
                        rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull()),
                        isNotLikedByUser
                )
                .orderBy(rawProduct.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(rawProduct.count())
                .from(rawProduct)
                .where(
                        rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull()),
                        isNotLikedByUser
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Long findMaxExposedRawProductIdExcludingLikedByUser(Long userId) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QJjym jjym = QJjym.jjym;
        QRecommendFurniture recommendFurniture = QRecommendFurniture.recommendFurniture;

        BooleanExpression isNotLikedByUser = JPAExpressions
                .selectOne()
                .from(jjym)
                .join(jjym.recommendFurniture, recommendFurniture)
                .where(
                        jjym.user.id.eq(userId),
                        recommendFurniture.source.eq(CurationSource.RAW),
                        recommendFurniture.furnitureProductId.eq(rawProduct.productId)
                )
                .notExists();

        return queryFactory
                .select(rawProduct.id.max())
                .from(rawProduct)
                .where(
                        rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull()),
                        isNotLikedByUser
                )
                .fetchOne();
    }

    @Override
    public List<CurationRawProduct> findExposedRawProductsExcludingLikedByUserWithCursor(
            Long userId,
            Long cursor,
            int size,
            List<Long> excludedIds
    ) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QJjym jjym = QJjym.jjym;
        QRecommendFurniture recommendFurniture = QRecommendFurniture.recommendFurniture;

        BooleanExpression isNotLikedByUser = JPAExpressions
                .selectOne()
                .from(jjym)
                .join(jjym.recommendFurniture, recommendFurniture)
                .where(
                        jjym.user.id.eq(userId),
                        recommendFurniture.source.eq(CurationSource.RAW),
                        recommendFurniture.furnitureProductId.eq(rawProduct.productId)
                )
                .notExists();

        BooleanBuilder where = new BooleanBuilder();
        where.and(rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull()));
        where.and(isNotLikedByUser);
        if (cursor != null) {
            where.and(rawProduct.id.lt(cursor));
        }
        if (excludedIds != null && !excludedIds.isEmpty()) {
            where.and(rawProduct.id.notIn(excludedIds.stream().filter(Objects::nonNull).toList()));
        }

        return queryFactory
                .selectFrom(rawProduct)
                .where(where)
                .orderBy(rawProduct.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<CurationRawProduct> findExposedRawProductsExcludingLikedByUserByCategory(
            Long userId,
            SoozipCategory category,
            int size,
            List<Long> excludedIds
    ) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        BooleanBuilder where = buildExposedRawProductBaseWhere(userId, excludedIds);
        if (category != null) {
            where.and(rawProduct.category.eq(category));
        }

        return queryFactory
                .selectFrom(rawProduct)
                .where(where)
                .orderBy(rawProduct.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<CurationRawProduct> findExposedRawProductsExcludingLikedByUserByFurnitureIds(
            Long userId,
            List<Long> furnitureIds,
            SoozipCategory category,
            int size,
            List<Long> excludedIds
    ) {
        if (furnitureIds == null || furnitureIds.isEmpty()) {
            return List.of();
        }

        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QCurationRawProductFurnitureTag mapping = QCurationRawProductFurnitureTag.curationRawProductFurnitureTag;
        QFurnitureTag furnitureTag = QFurnitureTag.furnitureTag;
        QFurniture furniture = QFurniture.furniture;

        BooleanBuilder where = buildExposedRawProductBaseWhere(userId, excludedIds);
        where.and(furniture.id.in(furnitureIds));
        if (category != null) {
            where.and(rawProduct.category.eq(category));
        }

        return queryFactory
                .selectDistinct(rawProduct)
                .from(rawProduct)
                .join(rawProduct.furnitureTagMappings, mapping)
                .join(mapping.furnitureTag, furnitureTag)
                .join(furnitureTag.furniture, furniture)
                .where(where)
                .orderBy(rawProduct.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<CurationRawProduct> findAllSimilarByFurnitureTypeIds(
            List<Long> furnitureTypeIds,
            List<Long> excludeRawProductIds,
            Pageable pageable
    ) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QCurationRawProductFurnitureTag mapping = QCurationRawProductFurnitureTag.curationRawProductFurnitureTag;
        QFurnitureTag furnitureTag = QFurnitureTag.furnitureTag;
        QFurniture furniture = QFurniture.furniture;
        QFurnitureType furnitureType = QFurnitureType.furnitureType;

        BooleanBuilder where = new BooleanBuilder();
        where.and(rawProduct.isExposed.isTrue());
        where.and(inFurnitureTypeIds(furnitureType.id, furnitureTypeIds));
        where.and(notInIds(rawProduct.id, excludeRawProductIds));

        return queryFactory
                .selectDistinct(rawProduct)
                .from(rawProduct)
                .join(rawProduct.furnitureTagMappings, mapping)
                .join(mapping.furnitureTag, furnitureTag)
                .join(furnitureTag.furniture, furniture)
                .join(furniture.furnitureType, furnitureType)
                .where(where)
                .orderBy(rawProduct.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<CurationRawProduct> findAllSimilarByTagIds(
            List<Long> tagIds,
            List<Long> excludeRawProductIds,
            Pageable pageable
    ) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QCurationRawProductFurnitureTag mapping = QCurationRawProductFurnitureTag.curationRawProductFurnitureTag;
        QFurnitureTag furnitureTag = QFurnitureTag.furnitureTag;
        QTag tag = QTag.tag;

        BooleanBuilder where = new BooleanBuilder();
        where.and(rawProduct.isExposed.isTrue());
        where.and(inIds(tag.id, tagIds));
        where.and(notInIds(rawProduct.id, excludeRawProductIds));

        return queryFactory
                .selectDistinct(rawProduct)
                .from(rawProduct)
                .join(rawProduct.furnitureTagMappings, mapping)
                .join(mapping.furnitureTag, furnitureTag)
                .join(furnitureTag.tag, tag)
                .where(where)
                .orderBy(rawProduct.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<CurationRawProduct> findAllSimilarByBrands(
            List<String> brands,
            List<Long> excludeRawProductIds,
            Pageable pageable
    ) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;

        BooleanBuilder where = new BooleanBuilder();
        where.and(rawProduct.isExposed.isTrue());
        where.and(inBrands(rawProduct.brand, brands));
        where.and(notInIds(rawProduct.id, excludeRawProductIds));

        return queryFactory
                .selectFrom(rawProduct)
                .where(where)
                .orderBy(rawProduct.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanBuilder buildExposedRawProductBaseWhere(Long userId, List<Long> excludedIds) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        BooleanBuilder where = new BooleanBuilder();
        where.and(rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull()));
        where.and(notLikedByUser(userId, rawProduct));
        if (excludedIds != null && !excludedIds.isEmpty()) {
            where.and(rawProduct.id.notIn(excludedIds.stream().filter(Objects::nonNull).toList()));
        }
        return where;
    }

    private BooleanExpression notLikedByUser(Long userId, QCurationRawProduct rawProduct) {
        QJjym jjym = QJjym.jjym;
        QRecommendFurniture recommendFurniture = QRecommendFurniture.recommendFurniture;

        return JPAExpressions
                .selectOne()
                .from(jjym)
                .join(jjym.recommendFurniture, recommendFurniture)
                .where(
                        jjym.user.id.eq(userId),
                        recommendFurniture.source.eq(CurationSource.RAW),
                        recommendFurniture.furnitureProductId.eq(rawProduct.productId)
                )
                .notExists();
    }

    @Override
    public Slice<CurationRawProduct> findAllByCurationFiltersRecommend(
            List<Long> typeIds,
            List<PriceRangeFilter> priceRanges,
            List<String> colorNames,
            Long cursor,
            Pageable pageable,
            List<Long> additionalProductIds
    ) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QCurationRawProductFurnitureTag mapping = QCurationRawProductFurnitureTag.curationRawProductFurnitureTag;
        QFurnitureTag fTag = QFurnitureTag.furnitureTag;
        QFurniture furniture = QFurniture.furniture;
        QFurnitureType fType = QFurnitureType.furnitureType;
        QCurationRawProductColor color = QCurationRawProductColor.curationRawProductColor;

        BooleanBuilder baseWhere = new BooleanBuilder();
        baseWhere.and(rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull()));
        if (cursor != null) {
            baseWhere.and(rawProduct.id.lt(cursor));
        }

        NumberExpression<Integer> matchScore = Expressions.asNumber(0);
        BooleanBuilder atLeastOne = new BooleanBuilder();

        if (typeIds != null && !typeIds.isEmpty()) {
            var typeSubquery = JPAExpressions
                    .select(mapping.curationRawProduct.id)
                    .from(mapping)
                    .join(mapping.furnitureTag, fTag)
                    .join(fTag.furniture, furniture)
                    .leftJoin(furniture.furnitureType, fType)
                    .where(fType.id.in(typeIds).or(furniture.id.in(typeIds)));

            matchScore = matchScore.add(
                    new CaseBuilder().when(rawProduct.id.in(typeSubquery)).then(1).otherwise(0)
            );
            atLeastOne.or(rawProduct.id.in(typeSubquery));
        }

        if (additionalProductIds != null && !additionalProductIds.isEmpty()) {
            matchScore = matchScore.add(
                    new CaseBuilder().when(rawProduct.id.in(additionalProductIds)).then(1).otherwise(0)
            );
            atLeastOne.or(rawProduct.id.in(additionalProductIds));
        }

        if (priceRanges != null && !priceRanges.isEmpty()) {
            BooleanBuilder priceCondition = new BooleanBuilder();
            for (PriceRangeFilter range : priceRanges) {
                BooleanBuilder rangeBuilder = new BooleanBuilder();
                if (range.min() != null) rangeBuilder.and(rawProduct.discountPrice.goe(range.min()));
                if (range.max() != null) rangeBuilder.and(rawProduct.discountPrice.loe(range.max()));
                priceCondition.or(rangeBuilder);
            }
            matchScore = matchScore.add(
                    new CaseBuilder().when(priceCondition).then(1).otherwise(0)
            );
            atLeastOne.or(priceCondition);
        }

        if (colorNames != null && !colorNames.isEmpty()) {
            BooleanBuilder colorCondition = new BooleanBuilder();
            for (String colorName : colorNames) {
                colorCondition.or(color.clientColorName.containsIgnoreCase(colorName));
            }
            var colorSubquery = JPAExpressions
                    .select(color.curationRawProduct.id)
                    .from(color)
                    .where(colorCondition);

            matchScore = matchScore.add(
                    new CaseBuilder().when(rawProduct.id.in(colorSubquery)).then(1).otherwise(0)
            );
            atLeastOne.or(rawProduct.id.in(colorSubquery));
        }

        baseWhere.and(atLeastOne);

        List<CurationRawProduct> content = queryFactory
                .selectFrom(rawProduct)
                .where(baseWhere)
                .orderBy(matchScore.desc(), rawProduct.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public List<Long> findEtcProductIds(Long selectiveTypeId, List<Long> excludedFurnitureIds, Long etcDirectFurnitureId) {
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QCurationRawProductFurnitureTag mapping = QCurationRawProductFurnitureTag.curationRawProductFurnitureTag;
        QFurnitureTag fTag = QFurnitureTag.furnitureTag;
        QFurniture furniture = QFurniture.furniture;
        QFurnitureType fType = QFurnitureType.furnitureType;
        QCurationRawProductFurniture directMapping = QCurationRawProductFurniture.curationRawProductFurniture;

        java.util.Set<Long> result = new java.util.HashSet<>();

        // FurnitureTag 경로: SELECTIVE 타입 하위 중 개별 필터 가구 제외
        if (selectiveTypeId != null) {
            com.querydsl.core.types.dsl.BooleanExpression typeCondition = fType.id.eq(selectiveTypeId);
            if (excludedFurnitureIds != null && !excludedFurnitureIds.isEmpty()) {
                typeCondition = typeCondition.and(furniture.id.notIn(excludedFurnitureIds));
            }
            List<Long> fromTag = queryFactory
                    .select(mapping.curationRawProduct.id)
                    .from(mapping)
                    .join(mapping.furnitureTag, fTag)
                    .join(fTag.furniture, furniture)
                    .join(furniture.furnitureType, fType)
                    .where(typeCondition)
                    .fetch();
            result.addAll(fromTag);
        }

        // 직접 매핑 경로: ETC Furniture에 바로 매핑된 상품
        if (etcDirectFurnitureId != null) {
            List<Long> fromDirect = queryFactory
                    .select(directMapping.curationRawProduct.id)
                    .from(directMapping)
                    .where(directMapping.furniture.id.eq(etcDirectFurnitureId))
                    .fetch();
            result.addAll(fromDirect);
        }

        return new java.util.ArrayList<>(result);
    }

    private BooleanExpression containsIgnoreCase(com.querydsl.core.types.dsl.StringPath path, String keyword) {
        return path.isNotNull().and(path.containsIgnoreCase(keyword));
    }

    private BooleanExpression inFurnitureTypeIds(com.querydsl.core.types.dsl.NumberPath<Long> field, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return field.in(ids);
    }

    private BooleanExpression notInIds(com.querydsl.core.types.dsl.NumberPath<Long> field, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return field.notIn(ids);
    }

    private BooleanExpression inIds(com.querydsl.core.types.dsl.NumberPath<Long> field, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return field.in(ids);
    }

    private BooleanExpression inBrands(com.querydsl.core.types.dsl.StringPath field, List<String> brands) {
        if (brands == null || brands.isEmpty()) {
            return null;
        }
        return field.in(brands);
    }
}
