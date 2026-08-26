package or.sopt.houme.compare.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.CompareCatalogItem;
import or.sopt.houme.compare.domain.port.out.CompareCatalogPort;
import or.sopt.houme.compare.infra.entity.CompareCatalogItemEntity;
import or.sopt.houme.compare.infra.repository.CompareCatalogItemRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompareCatalogPersistenceAdapter implements CompareCatalogPort {

    private final CompareCatalogItemRepository repository;

    @Override
    public CompareCatalogItem upsert(CompareCatalogItem item) {
        CompareCatalogItemEntity entity = repository.findByEbayItemId(item.ebayItemId())
                .map(existing -> toEntity(item).withId(existing.getId()))
                .orElseGet(() -> toEntity(item));
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<CompareCatalogItem> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    private CompareCatalogItemEntity toEntity(CompareCatalogItem item) {
        return CompareCatalogItemEntity.builder()
                .ebayItemId(item.ebayItemId())
                .title(item.title())
                .imageUrl(item.imageUrl())
                .priceUsd(item.priceUsd())
                .productUrl(item.productUrl())
                .soozipCategory(item.soozipCategory())
                .titleEmbedding(item.titleEmbedding())
                .build();
    }

    private CompareCatalogItem toDomain(CompareCatalogItemEntity e) {
        return new CompareCatalogItem(
                e.getId(), e.getEbayItemId(), e.getTitle(), e.getImageUrl(),
                e.getPriceUsd() != null ? e.getPriceUsd() : 0.0,
                e.getProductUrl(), e.getSoozipCategory(), e.getTitleEmbedding()
        );
    }
}
