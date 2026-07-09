package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;

import java.util.List;

public interface FurnitureTagRepositoryCustom {

    List<FurnitureTag> findAllByTagIdAndFurnitureIn(Long tagId, List<Furniture> furnitures);

    List<FurnitureTag> findAllByFurnitureTypeIdWithFurnitureAndTag(Long furnitureTypeId);
}
