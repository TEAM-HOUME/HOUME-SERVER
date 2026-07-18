package or.sopt.houme.carousel.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CarouselJpaRepository extends JpaRepository<CarouselJpaEntity, Long> {
}
