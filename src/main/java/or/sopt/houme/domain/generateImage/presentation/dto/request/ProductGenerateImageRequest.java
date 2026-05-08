package or.sopt.houme.domain.generateImage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductGenerateImageRequest(
        @NotNull(message = "floorPlanId는 필수입니다.")
        Long floorPlanId,
        @NotBlank(message = "floorPlanView는 필수입니다.")
        String floorPlanView,
        @NotNull(message = "isMirror는 필수입니다.")
        Boolean isMirror,
        @NotNull(message = "productIds는 필수입니다.")
        @Size(min = 1, max = 6, message = "상품은 최소 1개에서 최대 6개까지 입력 가능합니다.")
        List<@NotNull(message = "productIds의 각 요소는 null일 수 없습니다.") Long> productIds
) {
}
