package or.sopt.houme.domain.house.service;

import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;

public interface HouseService {

    // 집구조(주거형태, 공간구조, 평형옵션) 선택지 제공
    HouseOptionsResponse getHouseOptionsResponse();

}
