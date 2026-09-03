package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.ComparePresetDetail;

import java.util.Optional;

public interface GetPresetDetailPort {
    Optional<ComparePresetDetail> findById(Long presetId);
}
