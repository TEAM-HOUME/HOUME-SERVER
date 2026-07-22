package or.sopt.houme.domain.furniture.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminCurationRawProductExposureUpdateRequest(
        @NotEmpty(message = "rawProductIds는 비어 있을 수 없습니다.")
        List<@NotNull(message = "rawProductId는 null일 수 없습니다.") Long> rawProductIds,

        @NotNull(message = "isExposed는 필수 입력값입니다.")
        Boolean isExposed
) {
}
