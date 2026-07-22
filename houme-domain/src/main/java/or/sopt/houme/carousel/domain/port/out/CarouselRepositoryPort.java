package or.sopt.houme.carousel.domain.port.out;

import or.sopt.houme.carousel.domain.Carousel;

import java.util.List;
import java.util.Optional;

/**
 * 캐러셀 영속화 아웃바운드 포트.
 */
public interface CarouselRepositoryPort {

    List<Carousel> findAll();

    Optional<Carousel> findById(Long carouselId);
}
