package or.sopt.houme.domain.prompt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptFurnitureListDTO {

    private List<Long> furnitureIds;
}
