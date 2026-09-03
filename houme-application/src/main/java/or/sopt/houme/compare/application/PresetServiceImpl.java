package or.sopt.houme.compare.application;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.ComparePresetView;
import or.sopt.houme.compare.domain.port.in.PresetUseCase;
import or.sopt.houme.compare.domain.port.out.GetPresetListPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PresetServiceImpl implements PresetUseCase {

    private final GetPresetListPort getPresetListPort;

    @Transactional(readOnly = true)
    @Override
    public List<ComparePresetView> getPresets() {
        return getPresetListPort.findAll();
    }
}
