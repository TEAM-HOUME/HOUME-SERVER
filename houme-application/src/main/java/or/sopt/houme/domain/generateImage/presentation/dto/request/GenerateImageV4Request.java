package or.sopt.houme.domain.generateImage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GenerateImageV4Request(
        @NotNull(message = "도면 식별자는 필수입니다.")
        Long floorPlanId,
        @NotBlank(message = "floorPlanView는 필수입니다.")
        String floorPlanView,
        @NotNull(message = "좌우반전 여부는 필수입니다.")
        Boolean isMirror,
        @NotNull(message = "moodBoardIds는 필수입니다.")
        @Size(min = 1, max = 5, message = "무드보드는 최소 1개에서 5개까지 입력 가능합니다.")
        List<Long> moodBoardIds,
        @NotBlank(message = "주요 활동은 필수입니다.")
        String activity,
        @NotNull(message = "furnitureIds는 필수입니다.")
        @Size(max = 6, message = "최대 6개까지만 입력 가능합니다.")
        List<Long> furnitureIds
) {
}
