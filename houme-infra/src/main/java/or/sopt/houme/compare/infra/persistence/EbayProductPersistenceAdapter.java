package or.sopt.houme.compare.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.compare.domain.EbayProduct;
import or.sopt.houme.compare.domain.port.out.EbayProductPort;
import or.sopt.houme.compare.infra.entity.EbayProductJpaEntity;
import or.sopt.houme.compare.infra.repository.EbayProductRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EbayProductPersistenceAdapter implements EbayProductPort {

    private final EbayProductRepository repository;

    @Override
    public EbayProduct upsert(EbayProduct item) {
        EbayProductJpaEntity entity = repository.findByEbayItemId(item.ebayItemId())
                .map(existing -> toEntity(item).withId(existing.getId()))
                .orElseGet(() -> toEntity(item));
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<EbayProduct> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    private EbayProductJpaEntity toEntity(EbayProduct item) {
        return EbayProductJpaEntity.builder()
                .ebayItemId(item.ebayItemId())
                .title(item.title())
                .imageUrl(item.imageUrl())
                .priceUsd(item.priceUsd())
                .productUrl(item.productUrl())
                .soozipCategory(item.soozipCategory())
                .titleEmbedding(item.titleEmbedding())
                .imageEmbedding(item.imageEmbedding())
                .build();
    }

    private EbayProduct toDomain(EbayProductJpaEntity e) {
        return new EbayProduct(
                e.getId(), e.getEbayItemId(), e.getTitle(), e.getImageUrl(),
                e.getPriceUsd() != null ? e.getPriceUsd() : 0.0,
                e.getProductUrl(), e.getSoozipCategory(), e.getTitleEmbedding(), e.getImageEmbedding()
        );
    }
}
