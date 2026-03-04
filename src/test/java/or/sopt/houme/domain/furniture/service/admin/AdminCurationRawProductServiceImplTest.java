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
import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.SoozipCategory;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductCreateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.request.AdminCurationRawProductUpdateRequest;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductListResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.AdminCurationRawProductResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminCurationRawProductServiceImpl 테스트")
class AdminCurationRawProductServiceImplTest {

    @Mock
    private CurationRawProductRepository curationRawProductRepository;

    @Mock
    private CurationRawProductColorRepository curationRawProductColorRepository;

    @InjectMocks
    private AdminCurationRawProductServiceImpl adminCurationRawProductService;

    @Test
    @DisplayName("getAll()은 페이지 메타데이터와 상품 목록을 함께 반환한다")
    void getAll_success() {
        CurationRawProduct rawProduct = CurationRawProduct.of(
                "soozip",
                SoozipCategory.FURNITURE,
                3003L,
                "https://cdn.houme.kr/image3.jpg",
                "https://soozip.co.kr/product/3003",
                "목록 상품",
                "SOOZIP",
                LocalDateTime.now()
        );

        when(curationRawProductRepository.findAllByFilters(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(rawProduct)));

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
                LocalDateTime.of(2026, 2, 1, 10, 0)
        );

        when(curationRawProductRepository.findBySourceAndCategoryAndProductId("soozip", SoozipCategory.FURNITURE, 1001L))
                .thenReturn(Optional.empty());
        when(curationRawProductRepository.saveAndFlush(any(CurationRawProduct.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdminCurationRawProductResponse response = adminCurationRawProductService.create(request);

        assertEquals("soozip", response.source());
        assertEquals(SoozipCategory.FURNITURE, response.category());
        assertEquals(1001L, response.productId());
        assertEquals("테스트 침대", response.productName());
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
                null
        );

        CurationRawProduct existing = CurationRawProduct.of(
                "soozip",
                SoozipCategory.FURNITURE,
                1001L,
                "https://cdn.houme.kr/image.jpg",
                "https://soozip.co.kr/product/1001",
                "기존 침대",
                "SOOZIP",
                LocalDateTime.now()
        );

        when(curationRawProductRepository.findBySourceAndCategoryAndProductId("soozip", SoozipCategory.FURNITURE, 1001L))
                .thenReturn(Optional.of(existing));

        GeneralException exception = assertThrows(GeneralException.class, () -> adminCurationRawProductService.create(request));
        assertEquals(ErrorCode.DUPLICATE_CURATION_RAW_PRODUCT, exception.getErrorCode());
    }

    @Test
    @DisplayName("delete()는 색상 데이터를 먼저 삭제한 뒤 원본 상품을 삭제한다")
    void delete_success() {
        CurationRawProduct rawProduct = CurationRawProduct.of(
                "soozip",
                SoozipCategory.FURNITURE,
                2002L,
                "https://cdn.houme.kr/image2.jpg",
                "https://soozip.co.kr/product/2002",
                "삭제 대상 침대",
                "SOOZIP",
                LocalDateTime.now()
        );

        when(curationRawProductRepository.findById(1L)).thenReturn(Optional.of(rawProduct));

        adminCurationRawProductService.delete(1L);

        InOrder inOrder = inOrder(curationRawProductColorRepository, curationRawProductRepository);
        inOrder.verify(curationRawProductColorRepository).deleteAllByCurationRawProduct(rawProduct);
        inOrder.verify(curationRawProductRepository).delete(rawProduct);
        inOrder.verify(curationRawProductRepository).flush();
    }

    @Test
    @DisplayName("update()는 DB 유니크 제약 위반 시 중복 예외를 반환한다")
    void update_duplicateConstraint_throwsDuplicateError() {
        CurationRawProduct rawProduct = CurationRawProduct.of(
                "soozip",
                SoozipCategory.FURNITURE,
                4004L,
                "https://cdn.houme.kr/image4.jpg",
                "https://soozip.co.kr/product/4004",
                "수정 대상 상품",
                "SOOZIP",
                LocalDateTime.now()
        );

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
}
