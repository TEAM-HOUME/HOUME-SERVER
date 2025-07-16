package or.sopt.houme.domain.prompt.dto;

import java.util.List;

public record PromptFurnitureListDTO(
        List<Long> furnitureTagIds
) {

    public static PromptFurnitureListDTO of(List<Long> furnitureIds) {
        return new PromptFurnitureListDTO(furnitureIds);
    }
}