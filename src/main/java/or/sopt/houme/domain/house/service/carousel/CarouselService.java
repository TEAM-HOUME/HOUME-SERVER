package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.transaction.annotation.Transactional;

public interface CarouselService {
    GetCarouselListResponseDTO getCarousel(int page);
    GetCarouselListResponseDTO getCarouselV2(int page, User user);

    @Transactional
    void likeCarousel(User user, Long carouselId);

    @Transactional
    void hateCarousel(User user, Long carouselId);

    @Transactional
    void likeCarouselV2(User user, Long rawProductId);

}
