package or.sopt.houme.domain.user.presentation.admin.controller.dto.floorplan.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;

import java.util.List;

public record AdminFloorPlanUpdateRequest(
        @JsonAlias("floorPlanName")
        String name,
        List<@NotNull Form> forms,
        List<@NotNull Structure> structures,
        List<@NotNull Equilibrium> equilibriums,
        @NotBlank
        String floorPlanPrompt,
        @Valid
        List<AdminFloorPlanImageRequest> images
) {
}
