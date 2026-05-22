package or.sopt.houme.domain.house.presentation.carousel.controller.dto;

import java.util.List;

public record GetCarouselV2ListResponseDTO(
        List<GetCarouselResponseDTO> carousels,
        Long nextCursor
) {

    public static GetCarouselV2ListResponseDTO of(List<GetCarouselResponseDTO> carousels, Long nextCursor) {
        return new GetCarouselV2ListResponseDTO(carousels, nextCursor);
    }
}
