package or.sopt.houme.domain.house.service;

import or.sopt.houme.domain.house.presentation.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.presentation.dto.request.HouseSelectRequest;
import or.sopt.houme.domain.house.presentation.dto.response.HouseIdResponse;
import or.sopt.houme.domain.house.presentation.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.house.domain.House;
import or.sopt.houme.user.domain.User;

import java.util.List;

public interface HouseService {

    // 집 구조 리스트 반환
    HouseOptionsResponse getHouseOptionsResponse();

    // 집 구조 선택
    HouseIdResponse selectHouseOptions(User user, HouseSelectRequest houseSelectRequest);

    // 유저의 가장 최근 등록 집의 도면 조건
    LatestHouseConditionDTO findLatestHouse(User user);

    // house activity 업데이트
    House updateHouseActivity(Long houseId, Activity activity);

    House findHouseById(long houseId);

    // house prompt 저장
    void saveHousePrompt(Long houseId, String prompt);

    House createTemplateHouse(User user, Long bannerId, String prompt, Long floorPlanId, boolean isMirror);

    House createTemplateHouse(User user, Long bannerId, String prompt, Long floorPlanId, boolean isMirror, String selectedView);

    // 집 도면 매핑 저장
    void saveHouseFloorPlan(Long houseId, Long floorPlanId, boolean isMirror);

    void saveHouseFloorPlan(Long houseId, Long floorPlanId, boolean isMirror, String selectedView);

    // house와 furniture 저장
    void saveHouseFurniture(Long houseId, List<Long> furnitureIds);

    // house와 무드보드(taste) 저장
    void saveHouseTaste(Long houseId, List<Long> tasteIds);

    boolean getIsMirrorByHouseId(Long houseId);
}
