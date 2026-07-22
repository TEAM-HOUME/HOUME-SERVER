package or.sopt.houme.carousel.infra.persistence;

import lombok.RequiredArgsConstructor;
import or.sopt.houme.carousel.domain.Carousel;
import or.sopt.houme.carousel.domain.port.out.CarouselRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link CarouselRepositoryPort} 의 JPA 구현 어댑터.
 */
@Component
@RequiredArgsConstructor
public class CarouselPersistenceAdapter implements CarouselRepositoryPort {

    private final CarouselJpaRepository jpaRepository;

    @Override
    public List<Carousel> findAll() {
        return jpaRepository.findAll().stream().map(CarouselMapper::toDomain).toList();
    }

    @Override
    public Optional<Carousel> findById(Long carouselId) {
        return jpaRepository.findById(carouselId).map(CarouselMapper::toDomain);
    }
}
