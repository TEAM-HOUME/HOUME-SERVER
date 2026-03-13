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
import or.sopt.houme.domain.banner.model.vo.BannerStyleAnswerChip;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerStyleAnswerChipRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerMappedRawProductResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBannerServiceImplTest {

    @InjectMocks
    private AdminBannerServiceImpl adminBannerService;

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private AdminBannerSupport adminBannerSupport;

    @Test
    @DisplayName("create()는 배너를 생성한다")
    void create_success() {
        CurationRawProduct chipRawProduct = rawProduct(1L, 101L, "책상 A");
        Banner banner = Banner.create(
                BannerType.BANNER,
                "https://image",
                "배너 제목",
                "설명",
                "질문",
                "prompt",
                "[]"
        );
        ReflectionTestUtils.setField(banner, "id", 10L);
        ReflectionTestUtils.setField(banner, "createdAt", LocalDateTime.of(2026, 3, 13, 12, 0));
        ReflectionTestUtils.setField(banner, "updatedAt", LocalDateTime.of(2026, 3, 13, 12, 5));

        AdminBannerCreateRequest request = new AdminBannerCreateRequest(
                "https://image",
                "배너 제목",
                "설명",
                "질문",
                "prompt",
                List.of(new AdminBannerStyleAnswerChipRequest(1, "칩", 1L)),
                List.of(1L)
        );

        when(adminBannerSupport.normalizeStyleAnswerChips(request.styleAnswerChips()))
                .thenReturn(List.of(new BannerStyleAnswerChip(1, "칩", 1L)));
        when(adminBannerSupport.extractAllRawProductIds(any(), eq(List.of(1L)))).thenReturn(List.of(1L));
        when(adminBannerSupport.loadRequiredRawProducts(List.of(1L))).thenReturn(Map.of(1L, chipRawProduct));
        when(adminBannerSupport.normalizeRequired("https://image")).thenReturn("https://image");
        when(adminBannerSupport.normalizeRequired("배너 제목")).thenReturn("배너 제목");
        when(adminBannerSupport.normalizeRequired("설명")).thenReturn("설명");
        when(adminBannerSupport.normalizeRequired("질문")).thenReturn("질문");
        when(adminBannerSupport.normalizeRequired("prompt")).thenReturn("prompt");
        when(adminBannerSupport.toStyleAnswerChipsJson(any())).thenReturn("[{\"order\":1}]");
        when(adminBannerSupport.buildMappings(any(Banner.class), eq(List.of(1L)), eq(Map.of(1L, chipRawProduct))))
                .thenReturn(List.of());
        when(bannerRepository.saveAndFlush(any(Banner.class))).thenReturn(banner);
        when(bannerRepository.findByIdWithRawProducts(10L, BannerType.BANNER, true)).thenReturn(Optional.of(banner));
        when(adminBannerSupport.parseStyleAnswerChipsJson(any())).thenReturn(List.of(new BannerStyleAnswerChip(1, "칩", 1L)));
        when(adminBannerSupport.toMappedRawProductResponses(eq(banner), eq(Map.of(1L, chipRawProduct)))).thenReturn(List.of());

        AdminBannerResponse response = adminBannerService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.bannerTitle()).isEqualTo("배너 제목");
        assertThat(response.styleDescription()).isEqualTo("설명");
        verify(bannerRepository).saveAndFlush(any(Banner.class));
    }

    @Test
    @DisplayName("update()는 배너를 수정한다")
    void update_success() {
        CurationRawProduct rawProduct = rawProduct(3L, 303L, "조명 C");
        Banner banner = Banner.create(
                BannerType.BANNER,
                "https://old-image",
                "기존 제목",
                "기존 설명",
                "기존 질문",
                "기존 프롬프트",
                "[]"
        );
        ReflectionTestUtils.setField(banner, "id", 11L);
        ReflectionTestUtils.setField(banner, "createdAt", LocalDateTime.of(2026, 3, 13, 12, 0));
        ReflectionTestUtils.setField(banner, "updatedAt", LocalDateTime.of(2026, 3, 13, 12, 5));

        AdminBannerUpdateRequest request = new AdminBannerUpdateRequest(
                "https://new-image",
                "새 제목",
                "새 설명",
                "새 질문",
                "새 프롬프트",
                List.of(new AdminBannerStyleAnswerChipRequest(1, "새 칩", 3L)),
                List.of(3L)
        );

        when(bannerRepository.findByIdWithRawProducts(11L, BannerType.BANNER, true)).thenReturn(Optional.of(banner));
        when(adminBannerSupport.parseStyleAnswerChipsJson(any())).thenReturn(List.of());
        when(adminBannerSupport.normalizeStyleAnswerChips(request.styleAnswerChips()))
                .thenReturn(List.of(new BannerStyleAnswerChip(1, "새 칩", 3L)));
        when(adminBannerSupport.extractMappedRawProductIds(banner)).thenReturn(List.of());
        when(adminBannerSupport.extractAllRawProductIds(any(), eq(List.of(3L)))).thenReturn(List.of(3L));
        when(adminBannerSupport.loadRequiredRawProducts(List.of(3L))).thenReturn(Map.of(3L, rawProduct));
        when(adminBannerSupport.normalizeRequired(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(adminBannerSupport.toStyleAnswerChipsJson(any())).thenReturn("[{\"order\":1}]");
        when(adminBannerSupport.buildMappings(any(Banner.class), eq(List.of(3L)), any())).thenReturn(List.of());
        when(bannerRepository.saveAndFlush(any(Banner.class))).thenReturn(banner);
        when(adminBannerSupport.toMappedRawProductResponses(eq(banner), eq(Map.of(3L, rawProduct))))
                .thenReturn(List.of(AdminBannerMappedRawProductResponse.of(rawProduct)));

        AdminBannerResponse response = adminBannerService.update(11L, request);

        assertThat(response.bannerTitle()).isEqualTo("새 제목");
        assertThat(response.styleDescription()).isEqualTo("새 설명");
    }

    @Test
    @DisplayName("createImageUploadUrl()은 업로드 URL을 반환한다")
    void createImageUploadUrl_success() {
        when(adminBannerSupport.createImageUploadUrl(any(), eq("image/png"), eq("banner")))
                .thenReturn(new AdminBannerImageUploadResponse("https://upload", "https://public"));

        AdminBannerImageUploadResponse response = adminBannerService.createImageUploadUrl(
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

        AdminBannerRawProductSearchResponse response = adminBannerService.searchRawProducts("책상", 10);

        assertThat(response.rawProducts()).hasSize(1);
        assertThat(response.rawProducts().getFirst().productName()).isEqualTo("책상 A");
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
