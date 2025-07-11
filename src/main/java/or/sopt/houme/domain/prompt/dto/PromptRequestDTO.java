package or.sopt.houme.domain.prompt.dto;

import or.sopt.houme.domain.house.entity.enums.Equilibrium;


public record PromptRequestDTO(
        Long floorPlanId,
        boolean isMirror,   // isMirror가 true인 경우 반전 했음.
        Long tasteId,
        Equilibrium equilibrium,
        PromptFurnitureListDTO promptFurnitureListDTO
) {
    public static PromptRequestDTO of(Long floorPlanId,
                                      boolean isMirror,
                                      Long tasteId,
                                      Equilibrium equilibrium,
                                      PromptFurnitureListDTO promptFurnitureListDTO) {
        return new PromptRequestDTO(floorPlanId, isMirror, tasteId, equilibrium, promptFurnitureListDTO);
    }
}