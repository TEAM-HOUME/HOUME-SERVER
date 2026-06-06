package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselV2ListResponseDTO;
import or.sopt.houme.domain.user.model.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CarouselService {
    GetCarouselListResponseDTO getCarousel(int page);
    GetCarouselV2ListResponseDTO getCarouselV2(User user, List<Long> furnitureIds);

    @Transactional
    void likeCarousel(User user, Long carouselId);

    @Transactional
    void hateCarousel(User user, Long carouselId);

    @Transactional
    void likeCarouselV2WithLog(User user, Long rawProductId);

}
