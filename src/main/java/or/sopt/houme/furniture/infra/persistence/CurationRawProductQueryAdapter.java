package or.sopt.houme.furniture.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.furniture.domain.CurationRawProductColorView;
import or.sopt.houme.furniture.domain.CurationRawProductView;
import or.sopt.houme.furniture.domain.port.out.CurationRawProductQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link CurationRawProductQueryPort} 의 JPA 구현 어댑터. 원본상품 엔티티 그래프를 read model 로 변환해 반환한다.
 */
@Component
@RequiredArgsConstructor
public class CurationRawProductQueryAdapter implements CurationRawProductQueryPort {

    private final CurationRawProductRepository curationRawProductRepository;
    private final CurationRawProductColorRepository curationRawProductColorRepository;

    static CurationRawProductView toView(CurationRawProduct entity) {
        return CurationRawProductView.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .productImageUrl(entity.getProductImageUrl())
                .productSiteUrl(entity.getProductSiteUrl())
                .productMallName(entity.getProductMallName())
                .brand(entity.getBrand())
                .listPrice(entity.getListPrice())
                .discountRate(entity.getDiscountRate())
                .discountPrice(entity.getDiscountPrice())
                .fetchedAt(entity.getFetchedAt())
                .build();
    }

    @Override
    public Optional<CurationRawProductView> findById(Long id) {
        return curationRawProductRepository.findById(id).map(CurationRawProductQueryAdapter::toView);
    }

    @Override
    public List<CurationRawProductView> findAllByProductIdIn(List<Long> productIds) {
        return curationRawProductRepository.findAllByProductIdIn(productIds).stream()
                .map(CurationRawProductQueryAdapter::toView)
                .toList();
    }

    @Override
    public List<CurationRawProductColorView> findColorsByRawProductIdIn(List<Long> rawProductIds) {
        return curationRawProductColorRepository.findAllByCurationRawProductIdIn(rawProductIds).stream()
                .map(color -> new CurationRawProductColorView(
                        color.getCurationRawProduct().getId(),
                        color.getClientColorName(),
                        color.getRawColorName()
                ))
                .toList();
    }
}
