package or.sopt.houme.compare.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.ComparePresetView;
import or.sopt.houme.compare.domain.port.out.GetPresetListPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ComparePresetAdapter implements GetPresetListPort {

    private final ComparePresetJpaRepository presetRepository;

    @Override
    public List<ComparePresetView> findAll() {
        return presetRepository.findAll().stream()
                .map(e -> new ComparePresetView(e.getId(), e.getThumbnailUrl(), e.getTitle()))
                .toList();
    }
}
