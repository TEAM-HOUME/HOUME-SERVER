package or.sopt.houme.domain.house.repository;

import or.sopt.houme.house.infra.persistence.HouseJpaEntity;

import java.util.List;
import java.util.Optional;

public interface HouseCustomRepository {

    HouseJpaEntity findLatestHouse(Long userId);

    Optional<HouseJpaEntity> findHouseByUserIdAndImageId(Long userId, Long imageId);

    List<HouseJpaEntity> findValidHouseByUserId(Long userId);
}
