package or.sopt.houme.domain.house.dto;

import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;

public record LatestHouseConditionDTO(
        Form form,
        Structure structure,
        Equilibrium equilibrium
) {
}
