package or.sopt.houme.domain.house.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFurniture;
import or.sopt.houme.domain.house.model.entity.mapping.QHouseFurniture;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HouseFurnitureRepositoryImpl implements HouseFurnitureRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // #582: Furniture 연관 절단 — HouseFurniture 는 furnitureId(Long)만 보유하므로 furniture/furnitureType fetchJoin 제거.
    // 소비처는 furnitureId 로 직접(또는 필요 시 별도 조회) 접근한다.
    @Override
    public List<HouseFurniture> findAllByHouseIdWithFurniture(Long houseId) {
        QHouseFurniture houseFurniture = QHouseFurniture.houseFurniture;

        return queryFactory
                .selectFrom(houseFurniture)
                .where(houseFurniture.houseId.eq(houseId))
                .orderBy(houseFurniture.id.asc())
                .fetch();
    }

    @Override
    public List<HouseFurniture> findAllByHouseIdInWithFurniture(List<Long> houseIds) {
        if (houseIds == null || houseIds.isEmpty()) {
            return List.of();
        }

        QHouseFurniture houseFurniture = QHouseFurniture.houseFurniture;

        return queryFactory
                .selectFrom(houseFurniture)
                .where(houseFurniture.houseId.in(houseIds))
                .orderBy(houseFurniture.houseId.asc(), houseFurniture.id.asc())
                .fetch();
    }
}
