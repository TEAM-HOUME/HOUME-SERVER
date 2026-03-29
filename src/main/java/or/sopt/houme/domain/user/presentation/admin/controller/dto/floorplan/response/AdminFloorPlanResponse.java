package or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.response;

import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;

import java.util.List;

public record AdminFloorPlanResponse(
        Long id,
        String name,
        Form form,
        Structure structure,
        Equilibrium equilibrium,
        String floorPlanPrompt,
        String representativeImageUrl,
        List<AdminFloorPlanImageResponse> images
) {
}
