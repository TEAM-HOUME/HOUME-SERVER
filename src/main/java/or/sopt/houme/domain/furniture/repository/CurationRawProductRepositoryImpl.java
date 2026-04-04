package or.sopt.houme.domain.furniture.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QFurniture;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureType;
import or.sopt.houme.domain.house.model.taste.entity.QTag;
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
