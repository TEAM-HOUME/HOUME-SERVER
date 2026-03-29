package or.sopt.houme.domain.user.service.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.model.entity.BannerType;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.request.AdminLandingUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.landing.response.AdminLandingResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminLandingServiceImplTest {

    @InjectMocks
    private AdminLandingServiceImpl adminLandingService;

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private AdminBannerSupport adminBannerSupport;

    @Test
    @DisplayName("create()는 랜딩을 생성한다")
    void create_success() {
        Banner landing = Banner.create(BannerType.LANDING, "https://landing-image", "랜딩 제목", null, null, null, null);
        ReflectionTestUtils.setField(landing, "id", 30L);
        ReflectionTestUtils.setField(landing, "createdAt", LocalDateTime.of(2026, 3, 27, 12, 0));
        ReflectionTestUtils.setField(landing, "updatedAt", LocalDateTime.of(2026, 3, 27, 12, 5));

        AdminLandingCreateRequest request = new AdminLandingCreateRequest("https://landing-image", "랜딩 제목");
        when(adminBannerSupport.normalizeRequired(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bannerRepository.saveAndFlush(any(Banner.class))).thenReturn(landing);

        AdminLandingResponse response = adminLandingService.create(request);

        assertThat(response.id()).isEqualTo(30L);
        assertThat(response.bannerImageUrl()).isEqualTo("https://landing-image");
        assertThat(response.bannerTitle()).isEqualTo("랜딩 제목");
        verify(bannerRepository).saveAndFlush(any(Banner.class));
    }

    @Test
    @DisplayName("update()는 랜딩을 수정한다")
    void update_success() {
        Banner landing = Banner.create(BannerType.LANDING, "https://old-image", "기존 랜딩", null, null, null, null);
        ReflectionTestUtils.setField(landing, "id", 31L);
        ReflectionTestUtils.setField(landing, "createdAt", LocalDateTime.of(2026, 3, 27, 12, 0));
        ReflectionTestUtils.setField(landing, "updatedAt", LocalDateTime.of(2026, 3, 27, 12, 5));

        AdminLandingUpdateRequest request = new AdminLandingUpdateRequest("https://new-image", "새 랜딩");
        when(bannerRepository.findByIdWithRawProducts(31L, BannerType.LANDING, false)).thenReturn(Optional.of(landing));
        when(adminBannerSupport.normalizeRequired(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bannerRepository.saveAndFlush(any(Banner.class))).thenReturn(landing);

        AdminLandingResponse response = adminLandingService.update(31L, request);

        assertThat(response.bannerImageUrl()).isEqualTo("https://new-image");
        assertThat(response.bannerTitle()).isEqualTo("새 랜딩");
    }

    @Test
    @DisplayName("getAll()은 랜딩 목록을 반환한다")
    void getAll_success() {
        Banner landing = Banner.create(BannerType.LANDING, "https://landing-image", "랜딩 제목", null, null, null, null);
        ReflectionTestUtils.setField(landing, "id", 32L);
        ReflectionTestUtils.setField(landing, "createdAt", LocalDateTime.of(2026, 3, 27, 12, 0));
        ReflectionTestUtils.setField(landing, "updatedAt", LocalDateTime.of(2026, 3, 27, 12, 5));

        when(bannerRepository.findAllWithRawProducts(BannerType.LANDING, false)).thenReturn(List.of(landing));

        var response = adminLandingService.getAll();

        assertThat(response.landings()).hasSize(1);
        assertThat(response.landings().getFirst().id()).isEqualTo(32L);
    }

    @Test
    @DisplayName("createImageUploadUrl()은 업로드 URL을 반환한다")
    void createImageUploadUrl_success() {
        when(adminBannerSupport.createImageUploadUrl(any(), eq("image/png"), eq("landing")))
                .thenReturn(new AdminBannerImageUploadResponse("https://upload", "https://public"));

        AdminBannerImageUploadResponse response = adminLandingService.createImageUploadUrl(
                new AdminBannerImageUploadRequest("png"),
                "image/png"
        );

        assertThat(response.uploadUrl()).isEqualTo("https://upload");
        assertThat(response.publicUrl()).isEqualTo("https://public");
    }
}
