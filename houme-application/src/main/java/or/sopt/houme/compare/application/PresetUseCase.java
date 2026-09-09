package or.sopt.houme.compare.application;

import or.sopt.houme.compare.application.dto.PresetDetailResponse;
import or.sopt.houme.compare.application.dto.PresetListResponse;

public interface PresetUseCase {
    PresetListResponse getPresets();
    PresetDetailResponse getPresetDetail(Long presetId);
}
