package or.sopt.houme.domain.furniture.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurniture;
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
}
