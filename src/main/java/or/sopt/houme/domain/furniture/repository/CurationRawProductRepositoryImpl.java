package or.sopt.houme.domain.furniture.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.QFurniture;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureType;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepositoryCustom.PriceRangeFilter;
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
    public Page<CurationRawProduct> findAllByCurationFilters(
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

        // 1. 기본 노출 조건 및 커서 (필수 AND)
        finalWhere.and(rawProduct.isExposed.isTrue());
        if (cursor != null) {
            finalWhere.and(rawProduct.id.lt(cursor));
        }

        // 2. 키워드 검색 (AND)
        if (keyword != null && !keyword.isBlank()) {
            finalWhere.and(rawProduct.productName.containsIgnoreCase(keyword)
                    .or(rawProduct.brand.containsIgnoreCase(keyword)));
        }

        // 3. 가구 유형 필터 (types) - 그룹 내 OR
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

        // 4. 가격대 필터 (priceRanges) - 그룹 내 OR
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

        // 5. 색상 필터 (colors) - 그룹 내 OR
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
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(rawProduct.count())
                .from(rawProduct)
                .where(finalWhere)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private BooleanExpression containsIgnoreCase(com.querydsl.core.types.dsl.StringPath path, String keyword) {
        return path.isNotNull().and(path.containsIgnoreCase(keyword));
    }
}
