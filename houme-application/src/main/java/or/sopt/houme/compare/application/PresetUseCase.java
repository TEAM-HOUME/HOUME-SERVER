package or.sopt.houme.compare.application;

import or.sopt.houme.compare.application.dto.PresetDetailResponse;
import or.sopt.houme.compare.application.dto.PresetListResponse;
import or.sopt.houme.compare.application.dto.SavePresetRequest;

public interface PresetUseCase {
    PresetListResponse getPresets();
    PresetDetailResponse getPresetDetail(Long presetId);
    Long createPreset(SavePresetRequest request);
    void updatePreset(Long presetId, SavePresetRequest request);
    void deletePreset(Long presetId);
}
