package or.sopt.houme.domain.house.presentation.carousel.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetCarouselListResponseDTOTest {

    @Test
    @DisplayName("List<GetCarouselResponseDTO>로부터 GetCarouselListResponseDTO가 정상 생성된다")
    void of_createsValidResponseDTO() {
        // given
        GetCarouselResponseDTO dto1 = new GetCarouselResponseDTO(1L, "https://example.com/image1.jpg");
        GetCarouselResponseDTO dto2 = new GetCarouselResponseDTO(2L, "https://example.com/image2.jpg");
        List<GetCarouselResponseDTO> dtoList = List.of(dto1, dto2);

        // when
        GetCarouselListResponseDTO result = GetCarouselListResponseDTO.of(dtoList);

        // then
        assertThat(result.carouselResponseDTOS()).hasSize(2);
        assertThat(result.carouselResponseDTOS().get(0).rawProductId()).isEqualTo(1L);
        assertThat(result.carouselResponseDTOS().get(1).url()).isEqualTo("https://example.com/image2.jpg");
    }
}
