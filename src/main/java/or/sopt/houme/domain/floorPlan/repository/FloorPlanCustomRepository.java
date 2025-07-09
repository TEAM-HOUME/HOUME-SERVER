package or.sopt.houme.domain.floorPlan.repository;

import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;

import java.util.List;

public interface FloorPlanCustomRepository {

    // Form이랑 Structure 기반으로 도면 검색
    List<FloorPlan> findAllByFormAndStructure(Form form, Structure structure);
}
