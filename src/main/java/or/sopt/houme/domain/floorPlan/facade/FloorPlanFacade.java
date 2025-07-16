package or.sopt.houme.domain.floorPlan.facade;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanResponse;
import or.sopt.houme.domain.floorPlan.service.FloorPlanService;
import or.sopt.houme.domain.house.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

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
        List<FloorPlanResponse> housingPlan = floorPlanService.getHousingPlan(latestHouse.form(), latestHouse.structure());

        return new FloorPlanListResponse(housingPlan);
    }

}
