package or.sopt.houme.domain.furniture.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.Jjym;
import or.sopt.houme.domain.furniture.model.entity.QJjym;
import or.sopt.houme.domain.furniture.model.entity.QRecommendFurniture;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
