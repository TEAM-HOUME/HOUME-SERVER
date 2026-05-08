package or.sopt.houme.domain.furniture.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.QFurniture;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.QFurnitureType;
import or.sopt.houme.domain.house.model.taste.entity.QTag;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FurnitureTagRepositoryImpl implements FurnitureTagRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<FurnitureTag> findAllByTagIdAndFurnitureIn(Long tagId, List<Furniture> furnitures) {
        QFurnitureTag furnitureTag = QFurnitureTag.furnitureTag;

        return queryFactory
                .selectFrom(furnitureTag)
                .join(furnitureTag.furniture).fetchJoin()
                .where(
                        furnitureTag.tag.id.eq(tagId),
                        furnitureTag.furniture.in(furnitures)
                )
                .fetch();
    }

    @Override
    public List<FurnitureTag> findAllByFurnitureTypeIdWithFurnitureAndTag(Long furnitureTypeId) {
        QFurnitureTag furnitureTag = QFurnitureTag.furnitureTag;
        QFurniture furniture = QFurniture.furniture;
        QFurnitureType furnitureType = QFurnitureType.furnitureType;
        QTag tag = QTag.tag;

        return queryFactory
                .selectFrom(furnitureTag)
                .join(furnitureTag.furniture, furniture).fetchJoin()
                .join(furniture.furnitureType, furnitureType).fetchJoin()
                .join(furnitureTag.tag, tag).fetchJoin()
                .where(furnitureType.id.eq(furnitureTypeId))
                .orderBy(
                        furniture.furnitureNameKr.asc(),
                        furnitureTag.priority.asc(),
                        furnitureTag.id.asc()
                )
                .fetch();
    }
}
