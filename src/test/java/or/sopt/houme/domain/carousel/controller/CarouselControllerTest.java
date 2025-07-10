package or.sopt.houme.domain.carousel.controller;

import or.sopt.houme.domain.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.carousel.service.CarouselService;
import or.sopt.houme.domain.carousel.service.CarouselServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CarouselControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarouselServiceImpl carouselService;


    @Test
    @DisplayName("GET /api/v1/carousels?page=0 요청 시 5개 이하의 캐러셀 응답을 반환한다")
    void getCarousels_returnsCarouselList() throws Exception {
        // given
        List<GetCarouselResponseDTO> mockList = List.of(
                new GetCarouselResponseDTO(1L, "url1"),
                new GetCarouselResponseDTO(2L, "url2")
        );
        GetCarouselListResponseDTO responseDTO = new GetCarouselListResponseDTO(mockList);
        when(carouselService.getCarousel(0)).thenReturn(responseDTO);

        // when & then
        mockMvc.perform(get("/api/v1/carousels")
                        .param("page", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data.carouselResponseDTOS", hasSize(2)))
                .andExpect(jsonPath("$.data.carouselResponseDTOS[0].carouselId").value(1))
                .andExpect(jsonPath("$.data.carouselResponseDTOS[0].url").value("url1"))
                .andExpect(jsonPath("$.data.carouselResponseDTOS[1].carouselId").value(2))
                .andExpect(jsonPath("$.data.carouselResponseDTOS[1].url").value("url2"));
    }

}