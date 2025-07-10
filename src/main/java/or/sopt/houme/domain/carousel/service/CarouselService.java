package or.sopt.houme.domain.carousel.service;

import or.sopt.houme.domain.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;


public interface CarouselService {
    GetCarouselListResponseDTO getCarousel(int page);

    @Transactional
    void likeCarousel(User user, Long carouselId);

    @Transactional
    void hateCarousel(User user, Long carouselId);
}
