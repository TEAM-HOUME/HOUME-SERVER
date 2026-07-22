package or.sopt.houme.house.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.repository.HouseFloorPlanRepository;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.house.domain.FloorPlanCondition;
import or.sopt.houme.house.domain.port.out.HouseFloorPlanPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.HouseException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link HouseFloorPlanPort} 의 JPA 구현 어댑터. house/floorPlan @ManyToOne(infra 내부)은 관리참조로 해소.
 */
@Component
@RequiredArgsConstructor
public class HouseFloorPlanAdapter implements HouseFloorPlanPort {

    private final HouseFloorPlanRepository houseFloorPlanRepository;
    private final FloorPlanRepository floorPlanRepository;
    private final or.sopt.houme.domain.house.repository.HouseRepository houseRepository;

    @Override
    public void save(Long houseId, Long floorPlanId, boolean isMirror, String selectedView) {
        FloorPlan floorPlan = floorPlanRepository.findById(floorPlanId)
                .orElseThrow(() -> new HouseException(ErrorCode.NOT_FOUND_FLOOR_PLAN));

        HouseFloorPlan houseFloorPlan = HouseFloorPlan.builder()
                .house(houseRepository.getReferenceById(houseId))
                .floorPlan(floorPlan)
                .isReverse(isMirror)
                .selectedView(selectedView)
                .build();

        houseFloorPlanRepository.save(houseFloorPlan);
    }

    @Override
    public Optional<Boolean> findIsMirrorByHouseId(Long houseId) {
        return houseFloorPlanRepository.findHouseFloorPlanByHouseId(houseId)
                .map(HouseFloorPlan::isReverse);
    }

    @Override
    public Optional<FloorPlanCondition> findConditionByHouseId(Long houseId) {
        return houseFloorPlanRepository.findHouseFloorPlanByHouseId(houseId)
                .map(HouseFloorPlan::getFloorPlan)
                .map(fp -> new FloorPlanCondition(fp.getForm(), fp.getStructure(), fp.getEquilibrium()));
    }
}
