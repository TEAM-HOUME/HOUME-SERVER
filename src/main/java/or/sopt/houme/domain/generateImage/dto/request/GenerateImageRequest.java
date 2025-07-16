package or.sopt.houme.domain.generateImage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// 이미지 생성 Request
public record GenerateImageRequest(
        @NotNull(message = "houseId는 필수입니다.")
        Long houseId,
        @NotBlank(message = "평형 입력은 필수입니다.")
        String equilibrium,
        @NotNull(message = "도면 정보는 필수입니다.")
        FloorPlanInfo floorPlan,
        @NotNull(message = "moodBoardId는 필수입니다.")
        @Size(min = 1, max = 5, message = "무드보드는 최소 1개에서 5개까지 입력 가능합니다.")
        List<Long> moodBoardIds,        // tasteId
        @NotBlank(message = "주요 활동은 필수입니다.")
        String activity,
        @NotNull(message = "bedId는 필수입니다.")
        Long bedId,
        @Size(max = 3, message = "최대 3개까지만 입력 가능합니다.")
        // 선택 가구들 아이디
        List<Long> selectiveIds
) {

    public record FloorPlanInfo(
            @NotNull(message = "도면 식별자는 필수입니다.")
            Long floorPlanId,
            @NotNull(message = "좌우반전 여부는 필수입니다.")
            Boolean isMirror
    ) {}
}
