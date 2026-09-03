package or.sopt.houme.compare.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.ComparePresetDetail;
import or.sopt.houme.compare.domain.ComparePresetView;
import or.sopt.houme.compare.domain.port.out.GetPresetDetailPort;
import or.sopt.houme.compare.domain.port.out.GetPresetListPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ComparePresetAdapter implements GetPresetListPort, GetPresetDetailPort {

    private final ComparePresetJpaRepository presetRepository;
    private final ComparePresetItemJpaRepository itemRepository;

    @Override
    public List<ComparePresetView> findAll() {
        return presetRepository.findAll().stream()
                .map(e -> new ComparePresetView(e.getId(), e.getThumbnailUrl(), e.getTitle()))
                .toList();
    }

    @Override
    public Optional<ComparePresetDetail> findById(Long presetId) {
        return presetRepository.findById(presetId).map(preset -> {
            List<ComparePresetDetail.SimilarItem> items = itemRepository.findByPreset(preset).stream()
                    .map(i -> new ComparePresetDetail.SimilarItem(
                            i.getSource(), i.getProductId(), i.getTitle(), i.getImageUrl(),
                            i.getPrice(), i.getCurrency(), i.getSiteName(),
                            i.getProductUrl(), i.getPriceUpdatedAt()
                    ))
                    .toList();
            return new ComparePresetDetail(
                    preset.getSourceUrl(), preset.getTitle(), preset.getThumbnailUrl(),
                    preset.getBrand(), preset.getPrice(), preset.getCurrency(), items
            );
        });
    }
}
