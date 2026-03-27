package or.sopt.houme.domain.banner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.presentation.dto.response.LandingListResponse;
import or.sopt.houme.domain.banner.repository.BannerRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @InjectMocks
    private BannerServiceImpl bannerService;

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("getLandings()는 랜딩 이미지를 우선 반환한다")
    void getLandings_returnsLandingImageUrl() {
        Banner banner = Banner.create(
                BannerType.BANNER,
                "https://banner-image",
                "https://landing-image",
                "배너 제목",
                "설명",
                "질문",
                "prompt",
                "[]"
        );

        when(bannerRepository.findAllWithRawProducts(BannerType.BANNER, false)).thenReturn(List.of(banner));

        LandingListResponse response = bannerService.getLandings();

        assertThat(response.landings()).hasSize(1);
        assertThat(response.landings().getFirst().imageUrl()).isEqualTo("https://landing-image");
    }

    @Test
    @DisplayName("getLandings()는 랜딩 이미지가 없으면 기존 배너 이미지를 반환한다")
    void getLandings_fallbacksToBannerImageUrl() {
        Banner banner = Banner.create(
                BannerType.BANNER,
                "https://banner-image",
                null,
                "배너 제목",
                "설명",
                "질문",
                "prompt",
                "[]"
        );

        when(bannerRepository.findAllWithRawProducts(BannerType.BANNER, false)).thenReturn(List.of(banner));

        LandingListResponse response = bannerService.getLandings();

        assertThat(response.landings()).hasSize(1);
        assertThat(response.landings().getFirst().imageUrl()).isEqualTo("https://banner-image");
    }
}
