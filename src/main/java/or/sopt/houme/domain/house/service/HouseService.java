package or.sopt.houme.domain.house.service;

import or.sopt.houme.domain.house.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.dto.request.HouseSelectRequest;
import or.sopt.houme.domain.house.dto.request.IsLikeRequest;
import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.user.entity.User;

public interface HouseService {

    // 집구조(주거형태, 공간구조, 평형옵션) 선택지 제공
    HouseOptionsResponse getHouseOptionsResponse();

    // 집 구조 선택 서비스
    void selectHouseOptions(User user, HouseSelectRequest houseSelectRequest);

    // 가장 최근에 등록한 house 찾기
    LatestHouseConditionDTO findLatestHouse(User user);

    // house에 주요활동 저장하기
    House updateHouseActivity(Long houseId, Activity activity);

    // 생성된 이미지 선호도
    House findHouseById(long houseId);
}
