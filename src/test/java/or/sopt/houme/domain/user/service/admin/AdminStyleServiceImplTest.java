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
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerMappedRawProductResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.request.AdminStyleUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.style.response.AdminStyleResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStyleServiceImplTest {

    @InjectMocks
    private AdminStyleServiceImpl adminStyleService;

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private AdminBannerSupport adminBannerSupport;

    @Test
    @DisplayName("create()는 스타일을 생성한다")
    void create_success() {
        CurationRawProduct rawProduct = rawProduct(1L, 101L, "의자 A");
        Banner style = Banner.create(
                BannerType.STYLE,
                "https://image",
                "스타일 제목",
                "스타일 설명",
                null,
                "prompt",
                null
        );
        ReflectionTestUtils.setField(style, "id", 20L);
        ReflectionTestUtils.setField(style, "createdAt", LocalDateTime.of(2026, 3, 13, 12, 0));
        ReflectionTestUtils.setField(style, "updatedAt", LocalDateTime.of(2026, 3, 13, 12, 5));

        AdminStyleCreateRequest request = new AdminStyleCreateRequest(
                "https://image",
                "스타일 제목",
                "스타일 설명",
                "prompt",
                List.of(1L)
        );

        when(adminBannerSupport.loadRequiredRawProducts(List.of(1L))).thenReturn(Map.of(1L, rawProduct));
        when(adminBannerSupport.normalizeRequired(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(adminBannerSupport.buildMappings(any(Banner.class), eq(List.of(1L)), any())).thenReturn(List.of());
        when(bannerRepository.saveAndFlush(any(Banner.class))).thenReturn(style);
        when(bannerRepository.findByIdWithRawProducts(20L, BannerType.STYLE, false)).thenReturn(Optional.of(style));
        when(adminBannerSupport.toMappedRawProductResponses(eq(style), eq(Map.of(1L, rawProduct))))
                .thenReturn(List.of(AdminBannerMappedRawProductResponse.of(rawProduct)));

        AdminStyleResponse response = adminStyleService.create(request);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.bannerTitle()).isEqualTo("스타일 제목");
        assertThat(response.styleDescription()).isEqualTo("스타일 설명");
        verify(bannerRepository).saveAndFlush(any(Banner.class));
    }

    @Test
    @DisplayName("update()는 스타일을 수정한다")
    void update_success() {
        CurationRawProduct rawProduct = rawProduct(2L, 202L, "테이블 B");
        Banner style = Banner.create(
                BannerType.STYLE,
                "https://old-image",
                "기존 스타일",
                "기존 설명",
                null,
                "old prompt",
                null
        );
        ReflectionTestUtils.setField(style, "id", 21L);
        ReflectionTestUtils.setField(style, "createdAt", LocalDateTime.of(2026, 3, 13, 12, 0));
        ReflectionTestUtils.setField(style, "updatedAt", LocalDateTime.of(2026, 3, 13, 12, 5));

        AdminStyleUpdateRequest request = new AdminStyleUpdateRequest(
                null,
                "새 스타일",
                "새 설명",
                "new prompt",
                List.of(2L)
        );

        when(bannerRepository.findByIdWithRawProducts(21L, BannerType.STYLE, false)).thenReturn(Optional.of(style));
        when(adminBannerSupport.loadRequiredRawProducts(List.of(2L))).thenReturn(Map.of(2L, rawProduct));
        when(adminBannerSupport.normalizeRequired(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(adminBannerSupport.buildMappings(any(Banner.class), eq(List.of(2L)), any())).thenReturn(List.of());
        when(bannerRepository.saveAndFlush(any(Banner.class))).thenReturn(style);
        when(adminBannerSupport.toMappedRawProductResponses(eq(style), eq(Map.of(2L, rawProduct))))
                .thenReturn(List.of(AdminBannerMappedRawProductResponse.of(rawProduct)));

        AdminStyleResponse response = adminStyleService.update(21L, request);

        assertThat(response.bannerTitle()).isEqualTo("새 스타일");
        assertThat(response.styleDescription()).isEqualTo("새 설명");
    }

    @Test
    @DisplayName("createImageUploadUrl()은 스타일 이미지 업로드 URL을 반환한다")
    void createImageUploadUrl_success() {
        when(adminBannerSupport.createImageUploadUrl(any(), eq("image/png"), eq("style")))
                .thenReturn(new AdminBannerImageUploadResponse("https://upload", "https://public"));

        AdminBannerImageUploadResponse response = adminStyleService.createImageUploadUrl(
                new AdminBannerImageUploadRequest("png"),
                "image/png"
        );

        assertThat(response.uploadUrl()).isEqualTo("https://upload");
        assertThat(response.publicUrl()).isEqualTo("https://public");
    }

    @Test
    @DisplayName("searchRawProducts()는 검색 결과를 반환한다")
    void searchRawProducts_success() {
        AdminBannerRawProductSearchResponse searchResponse = new AdminBannerRawProductSearchResponse(
                List.of(AdminBannerMappedRawProductResponse.of(rawProduct(1L, 10L, "책상 A")))
        );
        when(adminBannerSupport.searchRawProducts("책상", 10)).thenReturn(searchResponse);

        AdminBannerRawProductSearchResponse response = adminStyleService.searchRawProducts("책상", 10);

        assertThat(response.rawProducts()).hasSize(1);
    }

    private CurationRawProduct rawProduct(Long id, Long productId, String productName) {
        return CurationRawProduct.builder()
                .id(id)
                .source("soozip")
                .category(SoozipCategory.FURNITURE)
                .productId(productId)
                .productImageUrl("https://image/" + id)
                .productSiteUrl("https://site/" + id)
                .productName(productName)
                .productMallName("SOOZIP")
                .brand("브랜드")
                .fetchedAt(LocalDateTime.of(2026, 3, 13, 12, 0))
                .build();
    }
}
