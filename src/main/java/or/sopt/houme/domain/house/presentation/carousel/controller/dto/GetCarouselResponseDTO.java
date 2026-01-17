package or.sopt.houme.domain.house.presentation.carousel.controller.dto;

import or.sopt.houme.domain.house.model.carousel.entity.Carousel;

public record GetCarouselResponseDTO(
        Long carouselId,
        String url
)
{
    public static GetCarouselResponseDTO from(Carousel carousel) {
        return new GetCarouselResponseDTO(
                carousel.getId(),
                carousel.getUrl()
        );
    }

}
