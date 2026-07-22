package or.sopt.houme.house.domain.port.out;

import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;

import java.util.Optional;

/**
 * 도면 조회 아웃바운드 포트 (house 애플리케이션이 필요로 하는 최소 표면).
 */
public interface FloorPlanQueryPort {

    boolean existsById(Long floorPlanId);

    Optional<Long> findFirstIdByCondition(Form form, Structure structure, Equilibrium equilibrium);
}
