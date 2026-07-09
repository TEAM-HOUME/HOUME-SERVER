package or.sopt.houme.domain.house.presentation.carousel.controller;

import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselV2ListResponseDTO;
import or.sopt.houme.domain.house.service.carousel.CarouselLikeLogService;
import or.sopt.houme.domain.house.service.carousel.facade.CarouselOptimisticLockFacade;
import or.sopt.houme.domain.house.service.carousel.CarouselService;
import or.sopt.houme.domain.house.service.carousel.CarouselServiceImpl;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.model.entity.*;
import or.sopt.houme.global.api.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CarouselControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarouselServiceImpl carouselService;

    @MockBean
    private CarouselOptimisticLockFacade carouselOptimisticLockFacade;

    @MockBean
    private CarouselLikeLogService carouselLikeLogService;

    private CustomUserDetails testUserDetails;


    @BeforeEach
    void initUser() {
        User user = User.builder()
                .name("테스트유저")
                .email("test" + UUID.randomUUID() + "@example.com")
                .password("encoded-password")
                .birthday(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .hasGeneratedImage(false)
                .build();

        testUserDetails = new CustomUserDetails(user);
    }


    private void setAuthentication(CustomUserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    @Test
    @DisplayName("GET /api/v1/carousels?page=0 요청 시 5개 이하의 캐러셀 응답을 반환한다")
    @WithMockUser()
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
                .andExpect(jsonPath("$.data.carouselResponseDTOS[0].rawProductId").value(1))
                .andExpect(jsonPath("$.data.carouselResponseDTOS[0].url").value("url1"))
                .andExpect(jsonPath("$.data.carouselResponseDTOS[1].rawProductId").value(2))
                .andExpect(jsonPath("$.data.carouselResponseDTOS[1].url").value("url2"));
    }

    @Test
    @DisplayName("GET /api/v2/carousels 요청 시 100개 이하의 캐러셀 응답을 반환한다")
    @WithMockUser()
    void getCarouselsV2_returnsCarouselList() throws Exception {
        List<GetCarouselResponseDTO> mockList = List.of(
                new GetCarouselResponseDTO(11L, "url11"),
                new GetCarouselResponseDTO(12L, "url12")
        );
        GetCarouselV2ListResponseDTO responseDTO = GetCarouselV2ListResponseDTO.of(mockList);
        when(carouselService.getCarouselV2(any())).thenReturn(responseDTO);

        setAuthentication(testUserDetails);

        mockMvc.perform(get("/api/v2/carousels")
                        .requestAttr("userDetails", testUserDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").exists())
                .andExpect(jsonPath("$.data.carousels", hasSize(2)))
                .andExpect(jsonPath("$.data.carousels[0].rawProductId").value(11))
                .andExpect(jsonPath("$.data.carousels[0].url").value("url11"))
                .andExpect(jsonPath("$.data.carousels[1].rawProductId").value(12))
                .andExpect(jsonPath("$.data.carousels[1].url").value("url12"));
    }

    @Test
    @DisplayName("POST /api/v1/carousels/like 요청으로 좋아요를 추가 할 수 있다")
    void likeCarousel_success() throws Exception {
        // given
        Long carouselId = 123L;
        setAuthentication(testUserDetails);

        // when & then
        mockMvc.perform(post("/api/v1/carousels/like")
                        .param("carouselId", String.valueOf(carouselId))
                        .requestAttr("userDetails", testUserDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("캐러셀 좋아요가 정상적으로 저장되었습니다"));

        // verify
        Mockito.verify(carouselOptimisticLockFacade, Mockito.times(1))
                .likeCarousel(testUserDetails.getUser(), carouselId);
    }


    @Test
    @DisplayName("POST /api/v1/carousels/hate 요청 시 InterruptedException 발생 -> CarouselException으로 감싼다")
    void hateCarousel_shouldThrowCarouselException_whenInterrupted() throws Exception {
        // given
        Long carouselId = 456L;
        setAuthentication(testUserDetails);

        // InterruptedException 발생하도록 mock 설정
        doThrow(new InterruptedException("인터럽트 예외"))
                .when(carouselOptimisticLockFacade).hateCarousel(any(), eq(carouselId));

        // when & then
        mockMvc.perform(post("/api/v1/carousels/hate")
                        .param("carouselId", String.valueOf(carouselId))
                        .requestAttr("userDetails", testUserDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.CAROUSEL_INTERRUPT_EXCEPTION.getCode()))
                .andExpect(jsonPath("$.msg").value(ErrorCode.CAROUSEL_INTERRUPT_EXCEPTION.getMsg()));
    }


    @Test
    @DisplayName("POST /api/v1/carousels/hate 요청으로 싫어요를 처리 할 수 있다")
    void hateCarousel_success() throws Exception {
        // given
        Long carouselId = 456L;
        setAuthentication(testUserDetails);

        // when & then
        mockMvc.perform(post("/api/v1/carousels/hate")
                        .param("carouselId", String.valueOf(carouselId))
                        .requestAttr("userDetails", testUserDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("캐러셀 싫어요가 정상적으로 저장되었습니다"));

        // verify
        Mockito.verify(carouselOptimisticLockFacade, Mockito.times(1))
                .hateCarousel(testUserDetails.getUser(), carouselId);
    }


    @Test
    @DisplayName("POST /api/v1/carousels/like 요청 시 InterruptedException 발생 -> CarouselException으로 감싼다")
    void likeCarousel_shouldThrowCarouselException_whenInterrupted() throws Exception {
        // given
        Long carouselId = 456L;
        setAuthentication(testUserDetails);

        // InterruptedException 발생하도록 mock 설정
        doThrow(new InterruptedException("인터럽트 예외"))
                .when(carouselOptimisticLockFacade).likeCarousel(any(), eq(carouselId));

        // when & then
        mockMvc.perform(post("/api/v1/carousels/like")
                        .param("carouselId", String.valueOf(carouselId))
                        .requestAttr("userDetails", testUserDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ErrorCode.CAROUSEL_INTERRUPT_EXCEPTION.getCode()))
                .andExpect(jsonPath("$.msg").value(ErrorCode.CAROUSEL_INTERRUPT_EXCEPTION.getMsg()));
    }

    @Test
    @DisplayName("POST /api/v2/carousels/like 요청으로 좋아요를 추가하고 로그를 저장한다")
    void likeCarouselV2_success() throws Exception {
        Long rawProductId = 321L;
        setAuthentication(testUserDetails);

        mockMvc.perform(post("/api/v2/carousels/like")
                        .param("rawProductId", String.valueOf(rawProductId))
                        .requestAttr("userDetails", testUserDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("상품 찜이 정상적으로 저장되었습니다"));

        Mockito.verify(carouselOptimisticLockFacade, Mockito.times(1))
                .likeCarouselV2(testUserDetails.getUser(), rawProductId);
    }

    @Test
    @DisplayName("POST /api/v2/carousels/hate 요청으로 싫어요를 처리하고 로그를 저장한다")
    void hateCarouselV2_success() throws Exception {
        Long rawProductId = 654L;
        setAuthentication(testUserDetails);

        mockMvc.perform(post("/api/v2/carousels/hate")
                        .param("rawProductId", String.valueOf(rawProductId))
                        .requestAttr("userDetails", testUserDetails)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("응답 성공"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("캐러셀 싫어요 로그가 정상적으로 저장되었습니다"));

        Mockito.verify(carouselLikeLogService, Mockito.times(1))
                .createHateLog(testUserDetails.getUser(), rawProductId);
    }


}
