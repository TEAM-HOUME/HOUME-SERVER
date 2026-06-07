package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.model.entity.mapping.HouseFurniture;

import java.util.List;

public interface HouseFurnitureRepositoryCustom {
    List<HouseFurniture> findAllByHouseIdWithFurniture(Long houseId);

    List<HouseFurniture> findAllByHouseIdInWithFurniture(List<Long> houseIds);
}
