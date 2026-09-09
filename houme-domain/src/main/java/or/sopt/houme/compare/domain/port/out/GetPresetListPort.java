package or.sopt.houme.compare.domain.port.out;

import or.sopt.houme.compare.domain.ComparePresetView;

import java.util.List;

public interface GetPresetListPort {
    List<ComparePresetView> findAll();
}
