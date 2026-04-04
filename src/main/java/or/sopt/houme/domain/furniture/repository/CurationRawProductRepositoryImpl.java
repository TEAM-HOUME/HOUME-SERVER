package or.sopt.houme.domain.furniture.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QJjym;
import or.sopt.houme.domain.furniture.model.entity.QRecommendFurniture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
                        rawProduct.isExposed.isTrue(),
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
                        rawProduct.isExposed.isTrue(),
                        isNotLikedByUser
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression containsIgnoreCase(com.querydsl.core.types.dsl.StringPath path, String keyword) {
        return path.isNotNull().and(path.containsIgnoreCase(keyword));
    }
}
