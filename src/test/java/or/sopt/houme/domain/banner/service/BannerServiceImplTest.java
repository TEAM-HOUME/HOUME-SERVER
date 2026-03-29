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
    @DisplayName("getLandings()는 LANDING 타입 이미지만 반환한다")
    void getLandings_returnsLandingTypeBanners() {
        Banner landing = Banner.create(
                BannerType.LANDING,
                "https://landing-image",
                "랜딩 제목",
                null,
                null,
                null,
                null
        );

        when(bannerRepository.findAllWithRawProducts(BannerType.LANDING, false)).thenReturn(List.of(landing));

        LandingListResponse response = bannerService.getLandings();

        assertThat(response.landings()).hasSize(1);
        assertThat(response.landings().getFirst().name()).isEqualTo("랜딩 제목");
        assertThat(response.landings().getFirst().imageUrl()).isEqualTo("https://landing-image");
    }
}
