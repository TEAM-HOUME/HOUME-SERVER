package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.dto.PresetDetailResponse;
import or.sopt.houme.compare.application.dto.PresetListResponse;
import or.sopt.houme.compare.application.dto.SavePresetRequest;
import or.sopt.houme.compare.domain.port.out.GetPresetDetailPort;
import or.sopt.houme.compare.domain.port.out.GetPresetListPort;
import or.sopt.houme.compare.domain.port.out.SavePresetPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CompareException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PresetServiceImpl implements PresetUseCase {

    private final GetPresetListPort getPresetListPort;
    private final GetPresetDetailPort getPresetDetailPort;
    private final SavePresetPort savePresetPort;

    @Transactional(readOnly = true)
    @Override
    public PresetListResponse getPresets() {
        return PresetListResponse.from(getPresetListPort.findAll());
    }

    @Transactional(readOnly = true)
    @Override
    public PresetDetailResponse getPresetDetail(Long presetId) {
        return PresetDetailResponse.from(
                getPresetDetailPort.findById(presetId)
                        .orElseThrow(() -> new CompareException(ErrorCode.COMPARE_PRESET_NOT_FOUND))
        );
    }

    @Transactional
    @Override
    public Long createPreset(SavePresetRequest request) {
        return savePresetPort.create(request.toDomain());
    }

    @Transactional
    @Override
    public void updatePreset(Long presetId, SavePresetRequest request) {
        savePresetPort.update(presetId, request.toDomain());
    }

    @Transactional
    @Override
    public void deletePreset(Long presetId) {
        savePresetPort.delete(presetId);
    }
}
