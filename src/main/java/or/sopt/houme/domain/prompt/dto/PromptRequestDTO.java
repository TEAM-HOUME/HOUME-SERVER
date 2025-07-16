package or.sopt.houme.domain.prompt.dto;

import or.sopt.houme.domain.house.entity.enums.Equilibrium;


public record PromptRequestDTO(
        Long floorPlanId,
        Long tagId,
        Equilibrium equilibrium,
        PromptFurnitureListDTO promptFurnitureListDTO
) {
    public static PromptRequestDTO of(Long floorPlanId,
                                      Long tagId,
                                      Equilibrium equilibrium,
                                      PromptFurnitureListDTO promptFurnitureListDTO) {
        return new PromptRequestDTO(floorPlanId, tagId, equilibrium, promptFurnitureListDTO);
    }
}