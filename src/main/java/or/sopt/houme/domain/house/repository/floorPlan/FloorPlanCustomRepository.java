package or.sopt.houme.domain.house.repository.floorPlan;

import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;

import java.util.List;

public interface FloorPlanCustomRepository {

    // Form이랑 Structure 기반으로 도면 검색
    List<FloorPlan> findAllByFormAndStructure(Form form, Structure structure);

    // Structure 기반으로 도면 검색
    List<FloorPlan> findAllByStructure(Structure structure);
}
