package or.sopt.houme.domain.house.presentation.carousel.controller.dto;

import or.sopt.houme.domain.house.model.carousel.entity.Carousel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetCarouselResponseDTOTest {

    @Test
    @DisplayName("from 메서드를 통해 responseDTO 를 만들 수 있다")
    void fromCarousel_createsValidDTO() {
        // given
        Carousel carousel = Carousel.builder()
                .id(1L)
                .url("https://example.com/image.jpg")
                .filename("image")
                .originalFilename("image_original")
                .fileExtension("jpg")
                .build();

        // when
        GetCarouselResponseDTO dto = GetCarouselResponseDTO.from(carousel);

        // then
        assertThat(dto.carouselId()).isEqualTo(1L);
        assertThat(dto.url()).isEqualTo("https://example.com/image.jpg");
    }
}
