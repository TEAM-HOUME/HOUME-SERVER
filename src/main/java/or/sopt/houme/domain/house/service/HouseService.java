package or.sopt.houme.domain.house.service;

import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.house.presentation.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.presentation.dto.request.HouseSelectRequest;
import or.sopt.houme.domain.house.presentation.dto.response.HouseIdResponse;
import or.sopt.houme.domain.house.presentation.dto.response.HouseOptionsResponse;
import or.sopt.houme.house.infra.persistence.HouseJpaEntity;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.user.domain.User;
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
    HouseJpaEntity updateHouseActivity(Long houseId, Activity activity);

    // 생성된 이미지 가져오기 (가장 최신 1개)
    HouseJpaEntity findHouseById(long houseId);

    // 생성된 이미지 프롬프트 저장
    void saveHousePrompt(HouseJpaEntity house, String prompt);

    // 템플릿 기반 이미지 생성을 위한 house 저장
    HouseJpaEntity createTemplateHouse(User user, Banner banner, String prompt, Long floorPlanId, boolean isMirror);

    HouseJpaEntity createTemplateHouse(User user, Banner banner, String prompt, Long floorPlanId, boolean isMirror, String selectedView);

    // houseId와 floorPlan 저장
    void saveHouseFloorPlan(HouseJpaEntity house, Long floorPlanId, boolean isMirror);

    void saveHouseFloorPlan(HouseJpaEntity house, Long floorPlanId, boolean isMirror, String selectedView);

    // house와 furniture 저장
    void saveHouseFurniture(HouseJpaEntity house, List<Long> furnitureIds);

    // house와 무드보드(taste) 저장
    void saveHouseTaste(HouseJpaEntity house, List<Long> tasteIds);

    // houseFloorPlan isMirror 조회
    boolean getIsMirrorByHouseId(Long houseId);
}
