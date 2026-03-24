package or.sopt.houme.domain.explore.service;

import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.explore.presentation.dto.response.BannerExploreListResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExploreServiceImplTest {

    @Mock
    private BannerRepository bannerRepository;

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
        when(bannerRepository.findAll(Sort.by(Sort.Direction.ASC, "id")))
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
        when(bannerRepository.findAll(Sort.by(Sort.Direction.ASC, "id")))
                .thenReturn(List.of(firstBanner, secondBanner));

        GeneralException exception = assertThrows(GeneralException.class,
                () -> exploreService.getExploreBanners(5L));

        assertEquals(ErrorCode.NOT_FOUND_BANNER, exception.getErrorCode());
    }
}
