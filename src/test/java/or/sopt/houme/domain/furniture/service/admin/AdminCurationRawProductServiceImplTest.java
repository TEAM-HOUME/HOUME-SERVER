package or.sopt.houme.domain.furniture.service.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductExposureUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductFurnitureTagCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductFurnitureTagUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductFurnitureTagResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductFurnitureTagRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCurationRawProductServiceImpl 테스트")
class AdminCurationRawProductServiceImplTest {

    @Mock
    private CurationRawProductRepository curationRawProductRepository;

    @Mock
    private CurationRawProductColorRepository curationRawProductColorRepository;

    @Mock
    private CurationRawProductFurnitureTagRepository curationRawProductFurnitureTagRepository;

    @Mock
    private FurnitureTagRepository furnitureTagRepository;

    @InjectMocks
    private AdminCurationRawProductServiceImpl adminCurationRawProductService;

    @Test
    @DisplayName("getAll()은 페이지 메타데이터와 상품 목록을 함께 반환한다")
    void getAll_success() {
        CurationRawProduct rawProduct = rawProduct(1L, "목록 상품", true);

        when(curationRawProductRepository.findAllByFilters(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(rawProduct)));
        when(curationRawProductColorRepository.findAllByCurationRawProductIdIn(anyList())).thenReturn(List.of());
        when(curationRawProductFurnitureTagRepository.findAllByCurationRawProductIdInWithFurnitureTag(anyList()))
                .thenReturn(List.of());

        AdminCurationRawProductListResponse response = adminCurationRawProductService.getAll(
                0,
                20,
                SoozipCategory.FURNITURE,
                100000L,
                500000L
        );

        assertEquals(1, response.products().size());
        assertEquals(0, response.page());
        assertEquals(1L, response.totalElements());
        assertTrue(response.products().get(0).isExposed());
    }

    @Test
    @DisplayName("getAll()은 최소 가격이 최대 가격보다 크면 예외를 던진다")
    void getAll_invalidPriceRange_throwsException() {
        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> adminCurationRawProductService.getAll(0, 20, null, 500000L, 100000L)
        );

        assertEquals(ErrorCode.NOT_VALID_EXCEPTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("create()는 신규 원본 상품을 저장한다")
    void create_success() {
        AdminCurationRawProductCreateRequest request = new AdminCurationRawProductCreateRequest(
                "soozip",
                SoozipCategory.FURNITURE,
                1001L,
                "https://cdn.houme.kr/image.jpg",
                "https://soozip.co.kr/product/1001",
                "테스트 침대",
                "SOOZIP",
                "테스트브랜드",
                100000L,
                10,
                90000L,
                3000L,
                50000L,
                false,
                LocalDateTime.of(2026, 2, 1, 10, 0)
        );

        when(curationRawProductRepository.findBySourceAndCategoryAndProductId("soozip", SoozipCategory.FURNITURE, 1001L))
                .thenReturn(Optional.empty());
        when(curationRawProductRepository.saveAndFlush(any(CurationRawProduct.class)))
                .thenAnswer(invocation -> {
                    CurationRawProduct saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 10L);
                    return saved;
                });
        when(curationRawProductColorRepository.findAllByCurationRawProductIdIn(anyList())).thenReturn(List.of());
        when(curationRawProductFurnitureTagRepository.findAllByCurationRawProductIdInWithFurnitureTag(anyList()))
                .thenReturn(List.of());

        AdminCurationRawProductResponse response = adminCurationRawProductService.create(request);

        assertEquals("soozip", response.source());
        assertEquals(SoozipCategory.FURNITURE, response.category());
        assertEquals(1001L, response.productId());
        assertEquals("테스트 침대", response.productName());
        assertFalse(response.isExposed());
        verify(curationRawProductRepository).saveAndFlush(any(CurationRawProduct.class));
    }

    @Test
    @DisplayName("create()는 중복된 source/category/productId 조합이면 예외를 던진다")
    void create_duplicate_throwsException() {
        AdminCurationRawProductCreateRequest request = new AdminCurationRawProductCreateRequest(
                "soozip",
                SoozipCategory.FURNITURE,
                1001L,
                "https://cdn.houme.kr/image.jpg",
                "https://soozip.co.kr/product/1001",
                "테스트 침대",
                "SOOZIP",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        CurationRawProduct existing = rawProduct(1L, "기존 침대", true);
        ReflectionTestUtils.setField(existing, "productId", 1001L);

        when(curationRawProductRepository.findBySourceAndCategoryAndProductId("soozip", SoozipCategory.FURNITURE, 1001L))
                .thenReturn(Optional.of(existing));

        GeneralException exception = assertThrows(GeneralException.class, () -> adminCurationRawProductService.create(request));
        assertEquals(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT, exception.getErrorCode());
    }

    @Test
    @DisplayName("update()는 DB 유니크 제약 위반 시 중복 예외를 반환한다")
    void update_duplicateConstraint_throwsDuplicateError() {
        CurationRawProduct rawProduct = rawProduct(10L, "수정 대상 상품", true);
        ReflectionTestUtils.setField(rawProduct, "productId", 4004L);

        AdminCurationRawProductUpdateRequest request = new AdminCurationRawProductUpdateRequest(
                "soozip",
                SoozipCategory.FURNITURE,
                4004L,
                "https://cdn.houme.kr/image4.jpg",
                "https://soozip.co.kr/product/4004",
                "수정된 상품명",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(curationRawProductRepository.findById(10L)).thenReturn(Optional.of(rawProduct));
        when(curationRawProductRepository.findBySourceAndCategoryAndProductId("soozip", SoozipCategory.FURNITURE, 4004L))
                .thenReturn(Optional.empty());
        when(curationRawProductRepository.saveAndFlush(rawProduct))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        GeneralException exception = assertThrows(
                GeneralException.class,
                () -> adminCurationRawProductService.update(10L, request)
        );

        assertEquals(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT, exception.getErrorCode());
    }

    @Test
    @DisplayName("updateExposure()는 대상 상품들의 노출 여부를 일괄 수정한다")
    void updateExposure_success() {
        CurationRawProduct first = rawProduct(1L, "상품1", true);
        CurationRawProduct second = rawProduct(2L, "상품2", true);

        when(curationRawProductRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(first, second));

        adminCurationRawProductService.updateExposure(
                new AdminCurationRawProductExposureUpdateRequest(List.of(1L, 2L), false)
        );

        assertFalse(first.getIsExposed());
        assertFalse(second.getIsExposed());
        verify(curationRawProductRepository).saveAll(List.of(first, second));
        verify(curationRawProductRepository).flush();
    }

    @Test
    @DisplayName("createFurnitureTagMapping()은 새 가구 태그 매핑을 추가한다")
    void createFurnitureTagMapping_success() {
        CurationRawProduct rawProduct = rawProduct(1L, "상품1", true);
        FurnitureTag furnitureTag = furnitureTag(11L);

        when(curationRawProductRepository.findById(1L)).thenReturn(Optional.of(rawProduct));
        when(furnitureTagRepository.findById(11L)).thenReturn(Optional.of(furnitureTag));
        when(curationRawProductFurnitureTagRepository.existsByCurationRawProductAndFurnitureTag(rawProduct, furnitureTag)).thenReturn(false);
        when(curationRawProductFurnitureTagRepository.saveAndFlush(any(CurationRawProductFurnitureTag.class)))
                .thenAnswer(invocation -> {
                    CurationRawProductFurnitureTag mapping = invocation.getArgument(0);
                    ReflectionTestUtils.setField(mapping, "id", 101L);
                    return mapping;
                });

        AdminCurationRawProductFurnitureTagResponse response = adminCurationRawProductService.createFurnitureTagMapping(
                1L,
                new AdminCurationRawProductFurnitureTagCreateRequest(11L)
        );

        assertEquals(101L, response.mappingId());
        assertEquals(11L, response.furnitureTagId());
        assertEquals(5L, response.furnitureId());
    }

    @Test
    @DisplayName("updateFurnitureTagMapping()은 기존 매핑의 가구 태그를 교체한다")
    void updateFurnitureTagMapping_success() {
        CurationRawProduct rawProduct = rawProduct(1L, "상품1", true);
        FurnitureTag currentFurnitureTag = furnitureTag(11L);
        FurnitureTag nextFurnitureTag = furnitureTag(22L);
        CurationRawProductFurnitureTag mapping = CurationRawProductFurnitureTag.of(rawProduct, currentFurnitureTag);
        ReflectionTestUtils.setField(mapping, "id", 101L);

        when(curationRawProductFurnitureTagRepository.findByIdAndCurationRawProductId(101L, 1L))
                .thenReturn(Optional.of(mapping));
        when(furnitureTagRepository.findById(22L)).thenReturn(Optional.of(nextFurnitureTag));
        when(curationRawProductFurnitureTagRepository.existsByCurationRawProductAndFurnitureTag(rawProduct, nextFurnitureTag))
                .thenReturn(false);
        when(curationRawProductFurnitureTagRepository.saveAndFlush(mapping)).thenReturn(mapping);

        AdminCurationRawProductFurnitureTagResponse response = adminCurationRawProductService.updateFurnitureTagMapping(
                1L,
                101L,
                new AdminCurationRawProductFurnitureTagUpdateRequest(22L)
        );

        assertEquals(22L, response.furnitureTagId());
        assertEquals(101L, response.mappingId());
    }

    @Test
    @DisplayName("deleteFurnitureTagMapping()은 대상 매핑을 삭제한다")
    void deleteFurnitureTagMapping_success() {
        CurationRawProduct rawProduct = rawProduct(1L, "상품1", true);
        CurationRawProductFurnitureTag mapping = CurationRawProductFurnitureTag.of(rawProduct, furnitureTag(11L));
        ReflectionTestUtils.setField(mapping, "id", 101L);

        when(curationRawProductFurnitureTagRepository.findByIdAndCurationRawProductId(101L, 1L))
                .thenReturn(Optional.of(mapping));

        adminCurationRawProductService.deleteFurnitureTagMapping(1L, 101L);

        verify(curationRawProductFurnitureTagRepository).delete(mapping);
        verify(curationRawProductFurnitureTagRepository).flush();
    }

    @Test
    @DisplayName("delete()는 색상 데이터를 먼저 삭제한 뒤 원본 상품을 삭제한다")
    void delete_success() {
        CurationRawProduct rawProduct = rawProduct(1L, "삭제 대상 침대", true);

        when(curationRawProductRepository.findById(1L)).thenReturn(Optional.of(rawProduct));

        adminCurationRawProductService.delete(1L);

        InOrder inOrder = inOrder(curationRawProductColorRepository, curationRawProductRepository);
        inOrder.verify(curationRawProductColorRepository).deleteAllByCurationRawProduct(rawProduct);
        inOrder.verify(curationRawProductRepository).delete(rawProduct);
        inOrder.verify(curationRawProductRepository).flush();
        verify(curationRawProductFurnitureTagRepository, never()).delete(any());
    }

    private CurationRawProduct rawProduct(Long id, String productName, boolean isExposed) {
        CurationRawProduct rawProduct = CurationRawProduct.of(
                "soozip",
                SoozipCategory.FURNITURE,
                3003L,
                "https://cdn.houme.kr/image3.jpg",
                "https://soozip.co.kr/product/3003",
                productName,
                "SOOZIP",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(rawProduct, "id", id);
        ReflectionTestUtils.setField(rawProduct, "isExposed", isExposed);
        return rawProduct;
    }

    private FurnitureTag furnitureTag(Long furnitureTagId) {
        Furniture furniture = Furniture.builder()
                .furnitureNameKr("침대")
                .furnitureNameEng("BED")
                .build();
        ReflectionTestUtils.setField(furniture, "id", 5L);

        Tag tag = Tag.of("minimal", 1, "미니멀", "prompt");
        ReflectionTestUtils.setField(tag, "id", 7L);

        FurnitureTag furnitureTag = FurnitureTag.builder()
                .furniturePrompt("prompt")
                .furniture(furniture)
                .tag(tag)
                .furnitureUrl("https://example.com/furniture")
                .searchKeyword("bed")
                .priority(1)
                .build();
        ReflectionTestUtils.setField(furnitureTag, "id", furnitureTagId);
        return furnitureTag;
    }
}
