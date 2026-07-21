package or.sopt.houme.domain.house.repository;

import or.sopt.houme.house.infra.persistence.HouseJpaEntity;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;

import java.util.List;
import java.util.Optional;

public interface HouseCustomRepository {

    HouseJpaEntity findLatestHouse(UserJpaEntity user);

    Optional<HouseJpaEntity> findHouseByUserIdAndImageId(Long userId, Long imageId);

    List<HouseJpaEntity> findValidHouseByUserId(Long userId);
}
