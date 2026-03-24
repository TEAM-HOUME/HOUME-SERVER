package or.sopt.houme.domain.explore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerDetailResponse;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExploreServiceImplTest {

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExploreServiceImpl exploreService;

    @Test
    @DisplayName("배너 전체 조회 시 요청한 bannerId부터 circular 순서로 반환한다")
    void getExploreBanners_returnsCircularOrder() {
        Banner firstBanner = Banner.builder().id(1L).bannerTitle("첫 번째").bannerImageUrl("https://google.com/1").build();
        Banner secondBanner = Banner.builder().id(2L).bannerTitle("두 번째").bannerImageUrl("https://google.com/2").build();
        Banner fifthBanner = Banner.builder().id(5L).bannerTitle("다섯 번째").bannerImageUrl("https://google.com/5").build();
        Banner seventhBanner = Banner.builder().id(7L).bannerTitle("일곱 번째").bannerImageUrl("https://google.com/7").build();
        Banner ninthBanner = Banner.builder().id(9L).bannerTitle("아홉 번째").bannerImageUrl("https://google.com/9").build();
        when(bannerRepository.findAllWithRawProducts(BannerType.BANNER, false))
                .thenReturn(List.of(firstBanner, secondBanner, fifthBanner, seventhBanner, ninthBanner));

        BannerExploreListResponse result = exploreService.getExploreBanners(5L);

        assertEquals(List.of(5L, 7L, 9L, 1L, 2L),
                result.banners().stream().map(banner -> banner.id()).toList());
    }

    @Test
    @DisplayName("배너 전체 조회 시 요청한 bannerId가 없으면 NOT_FOUND_BANNER 예외가 발생한다")
    void getExploreBanners_throwsWhenBannerIdNotFound() {
        Banner firstBanner = Banner.builder().id(1L).bannerTitle("첫 번째").bannerImageUrl("https://google.com/1").build();
        Banner secondBanner = Banner.builder().id(2L).bannerTitle("두 번째").bannerImageUrl("https://google.com/2").build();
        when(bannerRepository.findAllWithRawProducts(BannerType.BANNER, false))
                .thenReturn(List.of(firstBanner, secondBanner));

        GeneralException exception = assertThrows(GeneralException.class,
                () -> exploreService.getExploreBanners(5L));

        assertEquals(ErrorCode.NOT_FOUND_BANNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("배너 디테일 조회 시 BANNER 타입의 질문과 답변 목록을 반환한다")
    void getExploreBannerDetail_returnsBannerDetail() throws Exception {
        Banner banner = Banner.builder()
                .id(1L)
                .bannerType(BannerType.BANNER)
                .bannerTitle("잦은 재택근무하기 좋은 우리 집")
                .bannerImageUrl("https://google.com")
                .styleQuestion("업무 시 어떤 책상을 선호하시나요?")
                .styleAnswerChipsJson("[{\"order\":2},{\"order\":1}]")
                .build();
        when(bannerRepository.findByIdWithRawProducts(1L, BannerType.BANNER, false))
                .thenReturn(java.util.Optional.of(banner));
        when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(List.of(
                        new BannerStyleAnswerChip(2, "데스크테리어 가능한 깔끔한 책상", 12L),
                        new BannerStyleAnswerChip(1, "모니터 받침대가 결합된 책상", 11L)
                ));

        BannerDetailResponse result = exploreService.getExploreBannerDetail(1L);

        assertEquals("잦은 재택근무하기 좋은 우리 집", result.bannerName());
        assertEquals("https://google.com", result.bannerImageUrl());
        assertEquals("업무 시 어떤 책상을 선호하시나요?", result.question());
        assertEquals(List.of("모니터 받침대가 결합된 책상", "데스크테리어 가능한 깔끔한 책상"),
                result.answers().stream().map(answer -> answer.text()).toList());
    }

    @Test
    @DisplayName("배너 디테일 조회 시 BANNER 타입 배너가 없으면 NOT_FOUND_BANNER 예외가 발생한다")
    void getExploreBannerDetail_throwsWhenBannerNotFound() {
        when(bannerRepository.findByIdWithRawProducts(99L, BannerType.BANNER, false))
                .thenReturn(java.util.Optional.empty());

        GeneralException exception = assertThrows(GeneralException.class,
                () -> exploreService.getExploreBannerDetail(99L));

        assertEquals(ErrorCode.NOT_FOUND_BANNER, exception.getErrorCode());
    }
}
