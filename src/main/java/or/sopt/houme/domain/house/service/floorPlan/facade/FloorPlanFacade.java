package or.sopt.houme.domain.house.service.floorPlan.facade;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.house.service.floorPlan.FloorPlanService;
import or.sopt.houme.domain.house.presentation.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FloorPlanFacade {

    private final HouseService houseService;
    private final FloorPlanService floorPlanService;

    // 사용자 입력에 따른 도면들 제공
    public FloorPlanListResponse getFloorPlan(User user) {

        // 가장 최근 house 가져오기
        LatestHouseConditionDTO latestHouse = houseService.findLatestHouse(user);

        // 관련 도면 조회하기
        FloorPlanListResponse housingPlan = floorPlanService.getHousingPlan(latestHouse.form(), latestHouse.structure());

        return housingPlan;
    }

}
