package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.user.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface HouseCustomRepository {

    House findLatestHouse(User user);

    Optional<House> findHouseByUserIdAndImageId(Long userId, Long imageId);

    List<House> findValidHouseByUserId(Long userId);
}
