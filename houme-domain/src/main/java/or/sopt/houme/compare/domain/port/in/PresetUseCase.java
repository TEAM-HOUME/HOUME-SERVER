package or.sopt.houme.compare.domain.port.in;

import or.sopt.houme.compare.domain.ComparePresetView;

import java.util.List;

public interface PresetUseCase {
    List<ComparePresetView> getPresets();
}
