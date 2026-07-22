package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.furniture.infra.persistence.FurnitureJpaEntity;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;

import java.util.List;

public interface FurnitureTagRepositoryCustom {

    List<FurnitureTag> findAllByTagIdAndFurnitureIn(Long tagId, List<FurnitureJpaEntity> furnitures);

    List<FurnitureTag> findAllByFurnitureTypeIdWithFurnitureAndTag(Long furnitureTypeId);
}
