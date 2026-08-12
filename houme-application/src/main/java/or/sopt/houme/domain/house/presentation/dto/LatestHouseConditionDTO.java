package or.sopt.houme.domain.house.presentation.dto;

import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;

public record LatestHouseConditionDTO(
        Form form,
        Structure structure,
        Equilibrium equilibrium
) {
}
