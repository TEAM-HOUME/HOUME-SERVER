package or.sopt.houme.domain.user.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import or.sopt.houme.domain.banner.model.entity.Banner;
import or.sopt.houme.domain.banner.repository.BannerRepository;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerCreateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerImageUploadRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerStyleAnswerChipRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.request.AdminBannerUpdateRequest;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerImageUploadResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerRawProductSearchResponse;
import or.sopt.houme.domain.user.presentation.admin.controller.dto.banner.response.AdminBannerResponse;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import or.sopt.houme.global.dto.S3PresignedUrlResponseDTO;
import or.sopt.houme.global.util.S3PresignedUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBannerServiceImplTest {

    @InjectMocks
    private AdminBannerServiceImpl adminBannerService;

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private CurationRawProductRepository curationRawProductRepository;

    @Mock
    private S3PresignedUtil s3PresignedUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("create()는 스타일 배너를 생성하고 RAW 상품 매핑을 저장한다")
    void create_success() {
        adminBannerService = new AdminBannerServiceImpl(bannerRepository, curationRawProductRepository, objectMapper, s3PresignedUtil);
        CurationRawProduct chipRawProduct = rawProduct(1L, 101L, "책상 A");
        CurationRawProduct mappedRawProduct = rawProduct(2L, 202L, "의자 B");
        AtomicReference<Banner> savedBannerRef = new AtomicReference<>();

        AdminBannerCreateRequest request = new AdminBannerCreateRequest(
                "https://image",
                "배너 제목",
                "어떤 책상을 선호하시나요?",
                "mid century modern",
                List.of(new AdminBannerStyleAnswerChipRequest(1, "모니터 받침대가 결합된 책상", 1L)),
                List.of(1L, 2L)
        );

        when(curationRawProductRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(chipRawProduct, mappedRawProduct));
        when(bannerRepository.saveAndFlush(any(Banner.class))).thenAnswer(invocation -> {
            Banner banner = invocation.getArgument(0);
            ReflectionTestUtils.setField(banner, "id", 10L);
            ReflectionTestUtils.setField(banner, "createdAt", LocalDateTime.of(2026, 3, 13, 12, 0));
            ReflectionTestUtils.setField(banner, "updatedAt", LocalDateTime.of(2026, 3, 13, 12, 5));
            savedBannerRef.set(banner);
            return banner;
        });
        when(bannerRepository.findByIdWithRawProducts(10L)).thenAnswer(invocation -> Optional.ofNullable(savedBannerRef.get()));

        AdminBannerResponse response = adminBannerService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.bannerTitle()).isEqualTo("배너 제목");
        assertThat(response.styleAnswerChips()).hasSize(1);
        assertThat(response.styleAnswerChips().getFirst().curationRawProductName()).isEqualTo("책상 A");
        assertThat(response.mappedRawProducts()).hasSize(2);
        verify(bannerRepository, times(1)).saveAndFlush(any(Banner.class));
    }

    @Test
    @DisplayName("create()는 스타일 답변 칩이 4개를 초과하면 예외를 던진다")
    void create_invalidChipSize() {
        adminBannerService = new AdminBannerServiceImpl(bannerRepository, curationRawProductRepository, objectMapper, s3PresignedUtil);
        AdminBannerCreateRequest request = new AdminBannerCreateRequest(
                "https://image",
                "배너 제목",
                "질문",
                "prompt",
                List.of(
                        new AdminBannerStyleAnswerChipRequest(1, "a", 1L),
                        new AdminBannerStyleAnswerChipRequest(2, "b", 2L),
                        new AdminBannerStyleAnswerChipRequest(3, "c", 3L),
                        new AdminBannerStyleAnswerChipRequest(4, "d", 4L),
                        new AdminBannerStyleAnswerChipRequest(5, "e", 5L)
                ),
                List.of()
        );

        GeneralException exception = assertThrows(GeneralException.class, () -> adminBannerService.create(request));
        assertEquals(ErrorCode.NOT_VALID_EXCEPTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("update()는 배너 정보를 수정하고 매핑 가구를 교체한다")
    void update_success() throws Exception {
        adminBannerService = new AdminBannerServiceImpl(bannerRepository, curationRawProductRepository, objectMapper, s3PresignedUtil);
        Banner banner = Banner.create(
                "https://old-image",
                "기존 제목",
                "기존 질문",
                "기존 프롬프트",
                objectMapper.writeValueAsString(List.of())
        );
        ReflectionTestUtils.setField(banner, "id", 11L);
        ReflectionTestUtils.setField(banner, "createdAt", LocalDateTime.of(2026, 3, 13, 12, 0));
        ReflectionTestUtils.setField(banner, "updatedAt", LocalDateTime.of(2026, 3, 13, 12, 5));

        CurationRawProduct mappedRawProduct = rawProduct(3L, 303L, "조명 C");
        when(bannerRepository.findByIdWithRawProducts(11L)).thenReturn(Optional.of(banner));
        when(curationRawProductRepository.findAllById(List.of(3L))).thenReturn(List.of(mappedRawProduct));
        when(bannerRepository.saveAndFlush(any(Banner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminBannerUpdateRequest request = new AdminBannerUpdateRequest(
                "https://new-image",
                "새 제목",
                null,
                null,
                List.of(new AdminBannerStyleAnswerChipRequest(1, "포인트 조명", 3L)),
                List.of(3L)
        );

        AdminBannerResponse response = adminBannerService.update(11L, request);

        assertThat(response.bannerImageUrl()).isEqualTo("https://new-image");
        assertThat(response.bannerTitle()).isEqualTo("새 제목");
        assertThat(response.styleAnswerChips()).hasSize(1);
        assertThat(response.mappedRawProducts()).hasSize(1);
    }

    @Test
    @DisplayName("createImageUploadUrl()은 배너 이미지 업로드용 presigned url을 반환한다")
    void createImageUploadUrl_success() {
        adminBannerService = new AdminBannerServiceImpl(bannerRepository, curationRawProductRepository, objectMapper, s3PresignedUtil);
        when(s3PresignedUtil.createPresignedUrl("png", "banner", "image/png"))
                .thenReturn(new S3PresignedUrlResponseDTO("https://upload", "https://public", "banner/1.png", "banner"));

        AdminBannerImageUploadResponse response = adminBannerService.createImageUploadUrl(
                new AdminBannerImageUploadRequest("png"),
                "image/png"
        );

        assertThat(response.uploadUrl()).isEqualTo("https://upload");
        assertThat(response.publicUrl()).isEqualTo("https://public");
    }

    @Test
    @DisplayName("searchRawProducts()는 키워드로 RAW 상품을 검색한다")
    void searchRawProducts_success() {
        adminBannerService = new AdminBannerServiceImpl(bannerRepository, curationRawProductRepository, objectMapper, s3PresignedUtil);
        when(curationRawProductRepository.searchByKeyword(eq("책상"), eq(PageRequest.of(0, 10))))
                .thenReturn(new PageImpl<>(List.of(rawProduct(1L, 10L, "책상 A"))));

        AdminBannerRawProductSearchResponse response = adminBannerService.searchRawProducts("책상", 10);

        assertThat(response.rawProducts()).hasSize(1);
        assertThat(response.rawProducts().get(0).productName()).isEqualTo("책상 A");
    }

    private CurationRawProduct rawProduct(Long id, Long productId, String productName) {
        CurationRawProduct rawProduct = CurationRawProduct.builder()
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
        return rawProduct;
    }
}
