package or.sopt.houme.domain.furniture.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdminCurationRawProductFurnitureTagUpdateRequest(
        @NotNull(message = "furnitureTagId는 필수 입력값입니다.")
        @Positive(message = "furnitureTagId는 1 이상이어야 합니다.")
        Long furnitureTagId
) {
}
