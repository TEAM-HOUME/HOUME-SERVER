package or.sopt.houme.domain.house.repository;

import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.user.entity.User;

public interface HouseCustomRepository {

    House findLatestHouse(User user);
}
