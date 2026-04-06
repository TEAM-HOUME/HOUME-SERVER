package or.sopt.houme.domain.furniture.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CurationProductAppliedFilterResponse(
        String category, // type, price, color
        String id,
        String label,
        String value // Hex Code (only for color)
) {
}
