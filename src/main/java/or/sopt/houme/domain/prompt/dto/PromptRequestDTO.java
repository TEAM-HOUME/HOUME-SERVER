package or.sopt.houme.domain.prompt.dto;

import lombok.Getter;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;

@Getter
public class PromptRequestDTO {

    /**
     * 도면 식별자
     * 평형 식별자
     * 무드 식별자
     * 가구 식별자
     * */
    private Long floorPlanId;
    private Long tasteId;
    private Equilibrium equilibrium;

}
