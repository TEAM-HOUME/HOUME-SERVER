package or.sopt.houme.house.domain;

import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;

/** 집에 연결된 도면 조건 read model. */
public record FloorPlanCondition(Form form, Structure structure, Equilibrium equilibrium) {
}
