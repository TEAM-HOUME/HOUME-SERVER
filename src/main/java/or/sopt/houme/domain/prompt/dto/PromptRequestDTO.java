package or.sopt.houme.domain.prompt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptRequestDTO {

    private Long floorPlanId;
    private Long tasteId;
    private Equilibrium equilibrium;
    private PromptFurnitureListDTO promptFurnitureListDTO;
}
