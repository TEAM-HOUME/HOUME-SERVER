package or.sopt.houme.domain.coupang.service;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.coupang.domain.CoupangProduct;
import or.sopt.houme.coupang.domain.port.out.CoupangProductPort;
import or.sopt.houme.domain.coupang.repository.CoupangProductJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CoupangProductAdapter implements CoupangProductPort {

    private final CoupangProductJpaRepository repository;

    @Override
    public Optional<CoupangProduct> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<CoupangProduct> findAllByIdIn(List<Long> ids) {
        return repository.findAllById(ids).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private CoupangProduct toDomain(or.sopt.houme.domain.coupang.model.entity.CoupangProductJpaEntity e) {
        return new CoupangProduct(
                e.getId(),
                e.getName(),
                e.getImageUrl(),
                e.getProductUrl(),
                e.getCurrentPrice() != null ? e.getCurrentPrice().longValue() : null,
                e.getEstimatedOriginalPrice() != null ? e.getEstimatedOriginalPrice().longValue() : null,
                e.getDiscountRate() != null ? e.getDiscountRate().intValue() : null
        );
    }
}
