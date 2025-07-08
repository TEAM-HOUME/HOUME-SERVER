package or.sopt.houme.domain.house.service;

import or.sopt.houme.domain.house.dto.request.HouseSelectRequest;
import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.user.entity.User;

public interface HouseService {

    // 집구조(주거형태, 공간구조, 평형옵션) 선택지 제공
    HouseOptionsResponse getHouseOptionsResponse();

    // 집 구조 선택 서비스
    void selectHouseOptions(User user, HouseSelectRequest houseSelectRequest);
}
