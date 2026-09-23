package or.sopt.houme.domain.coupang.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.coupang.domain.CoupangProduct;
import or.sopt.houme.coupang.domain.port.out.CoupangProductPort;
import or.sopt.houme.domain.coupang.repository.CoupangProductJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CoupangProductAdapter implements CoupangProductPort {

    private final CoupangProductJpaRepository repository;

    @Override
    public Optional<CoupangProduct> findById(Long id) {
        return repository.findById(id).map(e -> new CoupangProduct(
                e.getId(),
                e.getName(),
                e.getImageUrl(),
                e.getProductUrl(),
                e.getCurrentPrice() != null ? e.getCurrentPrice().longValue() : null,
                e.getEstimatedOriginalPrice() != null ? e.getEstimatedOriginalPrice().longValue() : null,
                e.getDiscountRate() != null ? e.getDiscountRate().intValue() : null
        ));
    }
}
