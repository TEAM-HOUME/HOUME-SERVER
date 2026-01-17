package or.sopt.houme.domain.house.presentation.carousel.controller.dto;

import java.util.List;

public record GetCarouselListResponseDTO(List<GetCarouselResponseDTO> carouselResponseDTOS) {

    public static GetCarouselListResponseDTO of(List<GetCarouselResponseDTO> carouselResponseDTOS) {
        return new GetCarouselListResponseDTO(carouselResponseDTOS);
    }
}
