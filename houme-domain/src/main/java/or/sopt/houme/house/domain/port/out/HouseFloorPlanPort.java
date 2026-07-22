package or.sopt.houme.house.domain.port.out;

import or.sopt.houme.house.domain.FloorPlanCondition;

import java.util.Optional;

/**
 * 집-도면 매핑 아웃바운드 포트.
 */
public interface HouseFloorPlanPort {

    /** 매핑 저장. floorPlan 존재하지 않으면 구현이 도메인 예외를 던진다. */
    void save(Long houseId, Long floorPlanId, boolean isMirror, String selectedView);

    Optional<Boolean> findIsMirrorByHouseId(Long houseId);

    /** 집의 대표 도면 조건 (form/structure/equilibrium). */
    Optional<FloorPlanCondition> findConditionByHouseId(Long houseId);
}
