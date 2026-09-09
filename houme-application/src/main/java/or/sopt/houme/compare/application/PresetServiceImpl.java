package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.application.dto.PresetListResponse;
import or.sopt.houme.compare.domain.port.out.GetPresetListPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PresetServiceImpl implements PresetUseCase {

    private final GetPresetListPort getPresetListPort;

    @Transactional(readOnly = true)
    @Override
    public PresetListResponse getPresets() {
        return PresetListResponse.from(getPresetListPort.findAll());
    }
}
