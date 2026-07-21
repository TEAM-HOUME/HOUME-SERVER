package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselV2ListResponseDTO;
import or.sopt.houme.user.infra.persistence.UserJpaEntity;
import org.springframework.transaction.annotation.Transactional;

public interface CarouselService {
    GetCarouselListResponseDTO getCarousel(int page);
    GetCarouselV2ListResponseDTO getCarouselV2(UserJpaEntity user);

    @Transactional
    void likeCarousel(UserJpaEntity user, Long carouselId);

    @Transactional
    void hateCarousel(UserJpaEntity user, Long carouselId);

    @Transactional
    void likeCarouselV2WithLog(UserJpaEntity user, Long rawProductId);

}
