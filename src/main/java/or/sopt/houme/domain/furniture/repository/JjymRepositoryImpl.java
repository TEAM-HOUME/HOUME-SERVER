package or.sopt.houme.domain.furniture.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.Jjym;
import or.sopt.houme.domain.furniture.model.entity.QJjym;
import or.sopt.houme.domain.furniture.model.entity.QRecommendFurniture;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JjymRepositoryImpl implements JjymRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Jjym> findAllByUserIdWithFurnitureOrderByCreatedAtDesc(Long userId) {
        QJjym jjym = QJjym.jjym;
        QRecommendFurniture rf = QRecommendFurniture.recommendFurniture;

        return queryFactory
                .selectFrom(jjym)
                .join(jjym.recommendFurniture, rf).fetchJoin()
                .where(jjym.user.id.eq(userId))
                .orderBy(jjym.createdAt.desc())
                .fetch();
    }

    @Override
    public java.util.Optional<Jjym> findByUserIdAndRecommendFurnitureId(Long userId, Long recommendFurnitureId) {
        QJjym jjym = QJjym.jjym;
        QRecommendFurniture rf = QRecommendFurniture.recommendFurniture;

        Jjym result = queryFactory
                .selectFrom(jjym)
                .join(jjym.recommendFurniture, rf).fetchJoin()
                .where(
                        jjym.user.id.eq(userId),
                        jjym.recommendFurniture.id.eq(recommendFurnitureId)
                )
                .fetchOne();
        return java.util.Optional.ofNullable(result);
    }

    @Override
    public Map<Long, Long> countByRecommendFurnitureIds(List<Long> recommendFurnitureIds) {
        if (recommendFurnitureIds == null || recommendFurnitureIds.isEmpty()) {
            return Map.of();
        }

        QJjym jjym = QJjym.jjym;
        com.querydsl.core.types.dsl.NumberExpression<Long> jjymCountExpr = jjym.count();
        List<com.querydsl.core.Tuple> tuples = queryFactory
                .select(jjym.recommendFurniture.id, jjymCountExpr)
                .from(jjym)
                .where(jjym.recommendFurniture.id.in(recommendFurnitureIds))
                .groupBy(jjym.recommendFurniture.id)
                .fetch();

        Map<Long, Long> countMap = new HashMap<>();
        for (com.querydsl.core.Tuple tuple : tuples) {
            Long recommendFurnitureId = tuple.get(jjym.recommendFurniture.id);
            Long count = tuple.get(jjymCountExpr);
            if (recommendFurnitureId != null && count != null) {
                countMap.put(recommendFurnitureId, count);
            }
        }
        return countMap;
    }
}
