package or.sopt.houme.domain.house.service.carousel;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.service.JjymService;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselListResponseDTO;
import or.sopt.houme.domain.house.presentation.carousel.controller.dto.GetCarouselResponseDTO;
import or.sopt.houme.domain.house.model.carousel.entity.Carousel;
import or.sopt.houme.domain.house.model.carousel.entity.CarouselType;
import or.sopt.houme.domain.house.repository.carousel.CarouselRepository;
import or.sopt.houme.domain.preference.model.entity.CarouselPreference;
import or.sopt.houme.domain.preference.model.entity.Preference;
import or.sopt.houme.domain.preference.repository.CarouselPreferenceRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.CarouselException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarouselServiceImplTest {

    @InjectMocks
    private CarouselServiceImpl carouselService;

    @Mock
    private CarouselRepository carouselRepository;

    @Mock
    private PreferenceRepository preferenceRepository;

    @Mock
    private CarouselPreferenceRepository carouselPreferenceRepository;

    @Mock
    private CarouselCacheService carouselCacheService;

    @Mock
    private CurationRawProductRepository curationRawProductRepository;

    @Mock
    private JjymService jjymService;

    private User user;
    private Carousel carousel;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .build();

        carousel = Carousel.builder()
                .id(10L)
                .build();
    }


    @Test
    @DisplayName("getCarousel()는 페이지 번호에 맞는 캐러셀을 다섯개씩 반환한다")
    void getCarousel_returnsCorrectCarouselList() {
        // given
        int page = 0;
        CarouselType dummyType = CarouselType.builder().id(1L).build();

        List<Carousel> mockCarousels = List.of(
                Carousel.builder().id(1L).url("url1").filename("file1").originalFilename("origin1").fileExtension("png").carouselType(dummyType).build(),
                Carousel.builder().id(2L).url("url2").filename("file2").originalFilename("origin2").fileExtension("jpg").carouselType(dummyType).build()
        );

        GetCarouselResponseDTO from1 = GetCarouselResponseDTO.from(mockCarousels.get(0));
        GetCarouselResponseDTO from2 = GetCarouselResponseDTO.from(mockCarousels.get(1));

        List<GetCarouselResponseDTO> getCarouselResponseDTOS = List.of(from1, from2);

        when(carouselCacheService.getShuffledCarouselList()).thenReturn(getCarouselResponseDTOS);

        // when
        GetCarouselListResponseDTO result = carouselService.getCarousel(page);

        // then
        assertThat(result.carouselResponseDTOS()).hasSize(2);
        assertThat(result.carouselResponseDTOS())
                .extracting("carouselId")
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("getCarouselV2()는 사용자 찜 제외를 DB 쿼리에서 처리한 결과를 반환한다")
    void getCarouselV2_returnsExposedRawProductsExcludingLikedProducts() {
        CurationRawProduct rawProduct1 = CurationRawProduct.builder()
                .id(101L)
                .productImageUrl("image-101")
                .build();
        CurationRawProduct rawProduct2 = CurationRawProduct.builder()
                .id(102L)
                .productImageUrl("image-102")
                .build();

        when(curationRawProductRepository.findExposedRawProductsExcludingLikedByUser(1L, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(rawProduct1, rawProduct2), PageRequest.of(0, 5), 2));

        GetCarouselListResponseDTO result = carouselService.getCarouselV2(0, user);

        assertThat(result.carouselResponseDTOS()).hasSize(2);
        assertThat(result.carouselResponseDTOS().get(0).carouselId()).isEqualTo(101L);
        assertThat(result.carouselResponseDTOS().get(1).carouselId()).isEqualTo(102L);
        verify(curationRawProductRepository, times(1))
                .findExposedRawProductsExcludingLikedByUser(1L, PageRequest.of(0, 5));
        verifyNoInteractions(jjymService);
    }


    @Test
    @DisplayName("likeCarousel()은 carouselPreference 객체가 존재하면 새로운 preference와 관계를 생성한다")
    void likeCarousel_createsNew() {
        when(carouselRepository.findById(10L)).thenReturn(Optional.of(carousel));

        carouselService.likeCarousel(user, 10L);

        // 생성된 객체 저장 확인
        verify(preferenceRepository, times(1)).save(any(Preference.class));
        verify(carouselPreferenceRepository, times(1)).save(any(CarouselPreference.class));
    }


    @Test
    @DisplayName("likeCarousel()은 carouselPreference 객체가 기존에 존재하고 false인 경우 true로 변경한다")
    void likeCarousel_updatesExistingToTrue() {
        Preference pref = Preference.of(false);
        CarouselPreference cp = CarouselPreference.of(pref, carousel, 1L);

        when(carouselRepository.findById(10L)).thenReturn(Optional.of(carousel));
        when(carouselPreferenceRepository.findByUserIdAndCarouselId(1L, 10L)).thenReturn(Optional.of(cp));

        carouselService.likeCarousel(user, 10L);

        assertThat(pref.isLike()).isTrue();
    }


    @Test
    @DisplayName("존재하지 않는 캐러셀에 좋아요를 누를 경우 예외 발생")
    void likeCarousel_shouldThrowException_whenCarouselNotFound() {
        // given
        Long carouselId = 99L;
        when(carouselRepository.findById(carouselId)).thenReturn(Optional.empty());

        // when & then
        CarouselException exception = assertThrows(CarouselException.class, () ->
                carouselService.likeCarousel(user, carouselId));
        assertEquals(ErrorCode.CAROUSEL_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("likeCarousel()은 carouselPreference 객체가 기존에 존재하고 true라면 그대로 true로 둔다")
    void likeCarousel_updatesExistingToTrue_True() {
        Preference pref = Preference.of(true);
        CarouselPreference cp = CarouselPreference.of(pref, carousel, 1L);

        when(carouselRepository.findById(10L)).thenReturn(Optional.of(carousel));
        when(carouselPreferenceRepository.findByUserIdAndCarouselId(1L, 10L)).thenReturn(Optional.of(cp));

        carouselService.likeCarousel(user, 10L);

        assertThat(pref.isLike()).isTrue();
    }


    @Test
    @DisplayName("hateCarousel()은 carouselPreference 객체가 존재하지 않으면 새로운 preference를 false로 생성한다")
    void hateCarousel_createsNewFalse() {
        when(carouselRepository.findById(10L)).thenReturn(Optional.of(carousel));

        carouselService.hateCarousel(user, 10L);

        verify(preferenceRepository, times(1)).save(any(Preference.class));
        verify(carouselPreferenceRepository, times(1)).save(any(CarouselPreference.class));
    }



    @Test
    @DisplayName("hateCarousel()은 carouselPreference 객체가 기존에 true인 경우 false로 업데이트한다")
    void hateCarousel_updatesTrueToFalse() {
        Preference pref = Preference.of(true);
        CarouselPreference cp = CarouselPreference.of(pref, carousel, 1L);

        when(carouselRepository.findById(10L)).thenReturn(Optional.of(carousel));
        when(carouselPreferenceRepository.findByUserIdAndCarouselId(1L, 10L)).thenReturn(Optional.of(cp));

        carouselService.hateCarousel(user, 10L);

        assertThat(pref.isLike()).isFalse();
    }


    @Test
    @DisplayName("hateCarousel()은 carouselPreference 객체가 기존에 false인 경우 그대로 둔다")
    void hateCarousel_updatesTrueToFalse_false() {
        Preference pref = Preference.of(false);
        CarouselPreference cp = CarouselPreference.of(pref, carousel, 1L);

        when(carouselRepository.findById(10L)).thenReturn(Optional.of(carousel));
        when(carouselPreferenceRepository.findByUserIdAndCarouselId(1L, 10L)).thenReturn(Optional.of(cp));

        carouselService.hateCarousel(user, 10L);

        assertThat(pref.isLike()).isFalse();
    }


    @Test
    @DisplayName("존재하지 않는 캐러셀에 싫어요를 누를 경우 예외 발생")
    void hateCarousel_shouldThrowException_whenCarouselNotFound() {
        // given
        Long carouselId = 77L;
        when(carouselRepository.findById(carouselId)).thenReturn(Optional.empty());

        // when & then
        CarouselException exception = assertThrows(CarouselException.class, () ->
                carouselService.hateCarousel(user, carouselId));
        assertEquals(ErrorCode.CAROUSEL_NOT_FOUND, exception.getErrorCode());
    }


}
