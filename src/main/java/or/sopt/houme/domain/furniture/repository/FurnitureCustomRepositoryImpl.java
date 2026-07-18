package or.sopt.houme.domain.furniture.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.QFurniture;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureType;
import or.sopt.houme.domain.house.model.entity.mapping.QHouseFurniture;
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

        // #582: Tag 연관 절단 — furnitureTag.tag fetchJoin 제거(태그명은 tagId 로 별도 조회).
        return queryFactory
                .selectFrom(furniture)
                .leftJoin(furniture.furnitureTags, furnitureTag).fetchJoin()
                .orderBy(furniture.id.asc())
                .distinct()
                .fetch();
    }

    // N+1 검토 필요
    @Override
    public List<Furniture> findAllByHouseId(Long houseId) {
        QHouseFurniture houseFurniture = QHouseFurniture.houseFurniture;
        QFurniture furniture = QFurniture.furniture;

        // #582: HouseFurniture→Furniture 연관 절단 — id 명시 조인으로 재작성(furnitureId → furniture.id).
        return queryFactory
                .select(furniture)
                .distinct()
                .from(houseFurniture)
                .join(furniture).on(houseFurniture.furnitureId.eq(furniture.id))
                .where(houseFurniture.house.id.eq(houseId))
                .fetch();
    }

    @Override
    public List<Furniture> findAllWithFurnitureType() {
        QFurniture furniture = QFurniture.furniture;
        QFurnitureType furnitureType = QFurnitureType.furnitureType;

        return queryFactory
                .selectFrom(furniture)
                .join(furniture.furnitureType, furnitureType).fetchJoin() // Fetch Join
                .orderBy(furniture.id.asc())
                .fetch();
    }
}
