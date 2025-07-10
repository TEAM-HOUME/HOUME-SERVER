package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.user.entity.User;

import java.util.Optional;

public interface HouseCustomRepository {

    House findLatestHouse(User user);

    Optional<House> findHouseByUserIdAndImageId(Long userId, Long imageId);
}
