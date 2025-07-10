package or.sopt.houme.domain.carousel.service;

import or.sopt.houme.domain.carousel.controller.dto.GetCarouselListResponseDTO;


public interface CarouselService {
    GetCarouselListResponseDTO getCarousel(int page);
}
