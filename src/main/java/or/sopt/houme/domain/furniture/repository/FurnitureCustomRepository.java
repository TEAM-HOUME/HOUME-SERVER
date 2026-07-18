package or.sopt.houme.domain.furniture.repository;

import or.sopt.houme.furniture.infra.persistence.FurnitureJpaEntity;

import java.util.List;

public interface FurnitureCustomRepository {
    List<FurnitureJpaEntity> findAllWithTags();

    List<FurnitureJpaEntity> findAllByHouseId(Long houseId);

    // 가구와 가구 타입을 한 번에 가져오는 메서드
    List<FurnitureJpaEntity> findAllWithFurnitureType();
}
