package or.sopt.houme.domain.house.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.QFurniture;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureType;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFurniture;
import or.sopt.houme.domain.house.model.entity.mapping.QHouseFurniture;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HouseFurnitureRepositoryImpl implements HouseFurnitureRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HouseFurniture> findAllByHouseIdWithFurniture(Long houseId) {
        QHouseFurniture houseFurniture = QHouseFurniture.houseFurniture;
        QFurniture furniture = QFurniture.furniture;
        QFurnitureType furnitureType = QFurnitureType.furnitureType;

        return queryFactory
                .selectFrom(houseFurniture)
                .join(houseFurniture.furniture, furniture).fetchJoin()
                .join(furniture.furnitureType, furnitureType).fetchJoin()
                .where(houseFurniture.house.id.eq(houseId))
                .orderBy(houseFurniture.id.asc())
                .fetch();
    }
}
