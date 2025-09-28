package or.sopt.houme.domain.furniture.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.QFurniture;
import or.sopt.houme.domain.furniture.entity.QFurnitureTag;
import or.sopt.houme.domain.house.entity.QHouse;
import or.sopt.houme.domain.house.entity.mapping.QHouseFurniture;
import or.sopt.houme.domain.taste.entity.QTag;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FurnitureCustomRepositoryImpl implements FurnitureCustomRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * @implNote
     * furniture 와 연관된 데이터인 furnitureTag 와 tag 데이터를 fetch join 을 통해
     * 한 번에 영속성 컨텍스트에 올려놓는 메서드입니다
     *
     * 이를 통해 N+1을 방지할 수 있읍죠
     * */
    @Override
    public List<Furniture> findAllWithTags() {
        QFurniture furniture = QFurniture.furniture;
        QFurnitureTag furnitureTag = QFurnitureTag.furnitureTag;
        QTag tag = QTag.tag;

        return queryFactory
                .selectFrom(furniture)
                .leftJoin(furniture.furnitureTags, furnitureTag).fetchJoin()
                .leftJoin(furnitureTag.tag, tag).fetchJoin()
                .distinct()
                .fetch();
    }

    // N+1 검토 필요
    @Override
    public List<Furniture> findAllByHouseId(Long houseId) {
        QHouse house = QHouse.house;
        QHouseFurniture houseFurniture = QHouseFurniture.houseFurniture;
        QFurniture furniture = QFurniture.furniture;

        return queryFactory
                .select(furniture)
                .distinct()
                .from(houseFurniture)
                .join(houseFurniture.furniture, furniture)
                .join(houseFurniture.house, house)
                .where(house.id.eq(houseId))
                .fetch();
    }
}
