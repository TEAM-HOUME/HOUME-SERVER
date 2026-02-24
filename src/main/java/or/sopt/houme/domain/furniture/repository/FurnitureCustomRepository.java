package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.domain.furniture.model.entity.Furniture;

import java.util.List;

public interface FurnitureCustomRepository {
    List<Furniture> findAllWithTags();

    List<Furniture> findAllByHouseId(Long houseId);

    // 가구와 가구 타입을 한 번에 가져오는 메서드
    List<Furniture> findAllWithFurnitureType();
}
