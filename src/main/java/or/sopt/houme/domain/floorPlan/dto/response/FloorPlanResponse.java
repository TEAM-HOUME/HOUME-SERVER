package or.sopt.houme.domain.floorPlan.dto.response;

import lombok.Builder;
import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;

public record FloorPlanResponse(
        Long id,
        Form form,
        Structure structure,
        String floorPlanImage
) {

    public static FloorPlanResponse of(FloorPlan floorPlan){
        return new FloorPlanResponse(floorPlan.getId(), floorPlan.getForm(),
                floorPlan.getStructure(), floorPlan.getFloorPlanImage());
    }
}
