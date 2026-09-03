package or.sopt.houme.compare.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.ComparePresetDetail;
import or.sopt.houme.compare.domain.ComparePresetView;
import or.sopt.houme.compare.domain.port.out.GetPresetDetailPort;
import or.sopt.houme.compare.domain.port.out.GetPresetListPort;
import or.sopt.houme.compare.domain.port.out.SavePresetPort;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CompareException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ComparePresetAdapter implements GetPresetListPort, GetPresetDetailPort, SavePresetPort {

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

    @Transactional
    @Override
    public Long create(ComparePresetDetail detail) {
        ComparePresetJpaEntity preset = presetRepository.save(
                ComparePresetJpaEntity.builder()
                        .sourceUrl(detail.sourceUrl()).title(detail.title())
                        .thumbnailUrl(detail.thumbnailUrl()).brand(detail.brand())
                        .price(detail.price()).currency(detail.currency())
                        .build()
        );
        saveItems(preset, detail.similarProducts());
        return preset.getId();
    }

    @Transactional
    @Override
    public void update(Long presetId, ComparePresetDetail detail) {
        ComparePresetJpaEntity preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new CompareException(ErrorCode.COMPARE_PRESET_NOT_FOUND));
        preset.update(detail.title(), detail.thumbnailUrl(), detail.brand(), detail.price(), detail.currency());
        itemRepository.deleteAll(itemRepository.findByPreset(preset));
        saveItems(preset, detail.similarProducts());
    }

    @Transactional
    @Override
    public void delete(Long presetId) {
        ComparePresetJpaEntity preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new CompareException(ErrorCode.COMPARE_PRESET_NOT_FOUND));
        itemRepository.deleteAll(itemRepository.findByPreset(preset));
        presetRepository.delete(preset);
    }

    private void saveItems(ComparePresetJpaEntity preset, List<ComparePresetDetail.SimilarItem> items) {
        items.forEach(i -> itemRepository.save(
                ComparePresetItemJpaEntity.builder()
                        .preset(preset).source(i.source()).productId(i.productId())
                        .title(i.title()).imageUrl(i.imageUrl()).price(i.price())
                        .currency(i.currency()).siteName(i.siteName())
                        .productUrl(i.productUrl()).priceUpdatedAt(i.priceUpdatedAt())
                        .build()
        ));
    }
}
