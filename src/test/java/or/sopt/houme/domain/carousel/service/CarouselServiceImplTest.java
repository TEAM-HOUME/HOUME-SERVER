package or.sopt.houme.domain.carousel.service;

import or.sopt.houme.domain.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.carousel.entity.Carousel;
import or.sopt.houme.domain.carousel.repository.CarouselRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarouselServiceImplTest {

    @Mock
    private CarouselRepository carouselRepository;

    @InjectMocks
    private CarouselServiceImpl carouselService;

    @Test
    @DisplayName("getCarousel()는 페이지 번호에 맞는 캐러셀을 다섯개씩 반환한다")
    void getCarousel_returnsCorrectCarouselList() {
        // given
        int page = 0;
        Pageable pageable = PageRequest.of(page, 5);

        List<Carousel> mockCarousels = List.of(
                Carousel.builder().id(1L).url("url1").filename("file1").originalFilename("origin1").fileExtension("png").build(),
                Carousel.builder().id(2L).url("url2").filename("file2").originalFilename("origin2").fileExtension("jpg").build()
        );

        when(carouselRepository.findAll(pageable)).thenReturn(new PageImpl<>(mockCarousels));

        // when
        GetCarouselListResponseDTO result = carouselService.getCarousel(page);

        // then
        assertThat(result.carouselResponseDTOS()).hasSize(2);
        assertThat(result.carouselResponseDTOS().get(0).carouselId()).isEqualTo(1L);
        assertThat(result.carouselResponseDTOS().get(0).url()).isEqualTo("url1");
        assertThat(result.carouselResponseDTOS().get(1).carouselId()).isEqualTo(2L);
        assertThat(result.carouselResponseDTOS().get(1).url()).isEqualTo("url2");
    }
}