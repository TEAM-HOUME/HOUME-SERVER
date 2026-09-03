package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.ComparePresetDetail;

public interface SavePresetPort {
    Long create(ComparePresetDetail preset);

    void update(Long presetId, ComparePresetDetail preset);

    void delete(Long presetId);
}
