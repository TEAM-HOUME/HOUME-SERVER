package or.sopt.houme.domain.furniture.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProductFurnitureTag;
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
            Pageable pageable
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

        if (typeIds != null && !typeIds.isEmpty()) {
            finalWhere.and(rawProduct.id.in(
                    queryFactory.select(mapping.curationRawProduct.id)
                            .from(mapping)
                            .join(mapping.furnitureTag, fTag)
                            .join(fTag.furniture, furniture)
                            .leftJoin(furniture.furnitureType, fType)
                            .where(fType.id.in(typeIds).or(furniture.id.in(typeIds)))
            ));
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
