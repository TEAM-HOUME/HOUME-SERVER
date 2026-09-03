package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.ComparePresetDetail;
import or.sopt.houme.compare.domain.ComparePresetView;
import or.sopt.houme.compare.domain.port.in.PresetUseCase;
import or.sopt.houme.compare.domain.port.out.GetPresetDetailPort;
import or.sopt.houme.compare.domain.port.out.GetPresetListPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CompareException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PresetServiceImpl implements PresetUseCase {

    private final GetPresetListPort getPresetListPort;
    private final GetPresetDetailPort getPresetDetailPort;

    @Transactional(readOnly = true)
    @Override
    public List<ComparePresetView> getPresets() {
        return getPresetListPort.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public ComparePresetDetail getPresetDetail(Long presetId) {
        return getPresetDetailPort.findById(presetId)
                .orElseThrow(() -> new CompareException(ErrorCode.COMPARE_PRESET_NOT_FOUND));
    }
}
