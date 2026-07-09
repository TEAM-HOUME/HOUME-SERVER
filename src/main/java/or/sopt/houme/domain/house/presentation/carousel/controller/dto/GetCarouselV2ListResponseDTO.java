package or.sopt.houme.domain.house.presentation.carousel.controller.dto;

import java.util.List;

public record GetCarouselV2ListResponseDTO(
        List<GetCarouselResponseDTO> carousels
) {

    public static GetCarouselV2ListResponseDTO of(List<GetCarouselResponseDTO> carousels) {
        return new GetCarouselV2ListResponseDTO(carousels);
    }
}
