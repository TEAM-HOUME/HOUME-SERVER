package or.sopt.houme.carousel.infra.persistence;

import or.sopt.houme.carousel.domain.Carousel;

/**
 * 캐러셀 영속 엔티티 → 순수 도메인 모델 매퍼.
 */
public final class CarouselMapper {

    private CarouselMapper() {
    }

    public static Carousel toDomain(CarouselJpaEntity entity) {
        return Carousel.reconstitute(
                entity.getId(),
                entity.getUrl(),
                entity.getFilename(),
                entity.getOriginalFilename(),
                entity.getFileExtension(),
                entity.getCarouselType() != null ? entity.getCarouselType().getId() : null
        );
    }
}
