package or.sopt.houme.domain.house.service;

import or.sopt.houme.domain.house.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.dto.request.HouseSelectRequest;
import or.sopt.houme.domain.house.dto.response.HouseIdResponse;
import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HouseService {

    // 집구조(주거형태, 공간구조, 평형옵션) 선택지 제공
    HouseOptionsResponse getHouseOptionsResponse();

    // 집 구조 선택 서비스
    HouseIdResponse selectHouseOptions(User user, HouseSelectRequest houseSelectRequest);

    // 가장 최근에 등록한 house 찾기
    LatestHouseConditionDTO findLatestHouse(User user);

    // house에 주요활동 저장하기
    @Transactional
    House updateHouseActivity(Long houseId, Activity activity);

    // 생성된 이미지 가져오기 (가장 최신 1개)
    House findHouseById(long houseId);

    // 생성된 이미지 프롬프트 저장
    void saveHousePrompt(House house, String prompt);

    // houseId와 floorPlan 저장
    void saveHouseFloorPlan(House house, Long floorPlanId, boolean isMirror);

    // house와 furniture 저장
    void saveHouseFurniture(House house, List<Long> furnitureIds);

    // house와 무드보드(taste) 저장
    void saveHouseTaste(House house, List<Long> tasteIds);

    // houseFloorPlan isMirror 조회
    boolean getIsMirrorByHouseId(Long houseId);
}
