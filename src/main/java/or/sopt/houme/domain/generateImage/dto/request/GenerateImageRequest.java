package or.sopt.houme.domain.generateImage.dto.request;

import java.util.List;

// 이미지 생성 Request
public record GenerateImageRequest(
        Long houseId,
        String equilibrium,
        FloorPlanInfo floorPlan,
        Long moodBoardId,
        String activity,
        Long bedId,
        Long closetId,
        List<Long> selectiveIds
) {

    public record FloorPlanInfo(
            Long floorPlanId,
            boolean isMirror
    ) {}
}
