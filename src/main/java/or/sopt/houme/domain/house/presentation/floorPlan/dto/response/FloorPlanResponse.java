package or.sopt.houme.domain.house.presentation.floorPlan.dto.response;

import lombok.Builder;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;

public record FloorPlanResponse(
        Long id,
        Form form,
        Structure structure,
        String floorPlanImage
) {

    public static FloorPlanResponse of(FloorPlan floorPlan){
        return new FloorPlanResponse(floorPlan.getId(), floorPlan.getForm(),
                floorPlan.getStructure(), floorPlan.getUrl());
    }
}
