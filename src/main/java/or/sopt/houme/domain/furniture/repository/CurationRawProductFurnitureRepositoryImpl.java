package or.sopt.houme.domain.furniture.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.QCurationRawProductFurniture;
import or.sopt.houme.domain.furniture.model.entity.QFurniture;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CurationRawProductFurnitureRepositoryImpl implements CurationRawProductFurnitureRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CurationRawProductFurniture> findAllByCurationRawProductIdInWithFurniture(List<Long> rawProductIds) {
        QCurationRawProductFurniture mapping = QCurationRawProductFurniture.curationRawProductFurniture;
        QFurniture furniture = QFurniture.furniture;
        QFurnitureType furnitureType = QFurnitureType.furnitureType;

        return queryFactory
                .selectFrom(mapping)
                .join(mapping.furniture, furniture).fetchJoin()
                .join(furniture.furnitureType, furnitureType).fetchJoin()
                .where(mapping.curationRawProduct.id.in(rawProductIds))
                .fetch();
    }

    // [pbem22, 2026-05-28, #541] 선택된 가구 중 노출 가능한 상품이 매핑된 가구 ID 목록 반환
    @Override
    public List<Long> findFurnitureIdsHavingProducts(List<Long> furnitureIds) {
        if (furnitureIds == null || furnitureIds.isEmpty()) {
            return List.of();
        }

        QCurationRawProductFurniture mapping = QCurationRawProductFurniture.curationRawProductFurniture;
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;

        return queryFactory
                .select(mapping.furniture.id)
                .distinct()
                .from(mapping)
                .join(mapping.curationRawProduct, rawProduct)
                .where(
                        mapping.furniture.id.in(furnitureIds),
                        rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull())
                )
                .fetch();
    }

    // [pbem22, 2026-05-28, #541] furnitureId 기준 노출 가능한 원본 상품 매핑 목록 반환
    @Override
    public List<CurationRawProductFurniture> findExposedByFurnitureId(Long furnitureId) {
        QCurationRawProductFurniture mapping = QCurationRawProductFurniture.curationRawProductFurniture;
        QCurationRawProduct rawProduct = QCurationRawProduct.curationRawProduct;
        QFurniture furniture = QFurniture.furniture;

        return queryFactory
                .selectFrom(mapping)
                .join(mapping.curationRawProduct, rawProduct).fetchJoin()
                .join(mapping.furniture, furniture).fetchJoin()
                .where(
                        mapping.furniture.id.eq(furnitureId),
                        rawProduct.isExposed.isTrue().or(rawProduct.isExposed.isNull())
                )
                .fetch();
    }
}
