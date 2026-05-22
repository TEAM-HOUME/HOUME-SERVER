package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.CurationSource;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.model.entity.RecommendFurniture;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductListResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductColorRepository;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.furniture.repository.JjymRepository;
import or.sopt.houme.domain.furniture.repository.RecommendFurnitureRepository;
import or.sopt.houme.domain.user.model.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("상품 큐레이션 서비스 단위 테스트")
class CurationProductServiceImplTest {

    @InjectMocks
    private CurationProductServiceImpl curationProductService;

    @Mock
    private FurnitureTypeRepository furnitureTypeRepository;

    @Mock
    private FurnitureRepository furnitureRepository;

    @Mock
    private CurationRawProductRepository curationRawProductRepository;

    @Mock
    private CurationRawProductColorRepository curationRawProductColorRepository;

    @Mock
    private RecommendFurnitureRepository recommendFurnitureRepository;

    @Mock
    private JjymRepository jjymRepository;

    @Test
    @DisplayName("getFilterMetadata()는 DB 데이터와 정적 필터를 조합하여 반환한다")
    void getFilterMetadata() {
        // given
        FurnitureType bedType = FurnitureType.builder().id(1L).nameKr("침대").nameEng("BED").build();
        given(furnitureTypeRepository.findAll()).willReturn(List.of(bedType));
        given(furnitureRepository.findAll()).willReturn(List.of());

        // when
        CurationProductFilterResponse response = curationProductService.getFilterMetadata();

        // then
        assertThat(response).isNotNull();
        assertThat(response.furnitureTypes()).hasSize(12);
        assertThat(response.furnitureTypes().get(1).nameKr()).isEqualTo("침대/프레임");
    }

    @Test
    @DisplayName("getProducts()는 복합 필터 조건에 맞는 상품 목록과 메타데이터를 반환한다")
    void getProducts() {
        // given
        String keyword = "테스트";
        List<Long> typeIds = List.of(1L);
        List<String> priceRangeIds = List.of("P1");
        List<Long> colorIds = List.of(1L);
        Long cursor = 100L;
        Integer size = 20;

        FurnitureType bedType = FurnitureType.builder().id(1L).nameKr("침대").nameEng("BED").build();
        given(furnitureTypeRepository.findAll()).willReturn(List.of(bedType));
        given(furnitureRepository.findAll()).willReturn(List.of());

        CurationRawProduct product = CurationRawProduct.builder()
                .id(99L)
                .productId(3003L)
                .productName("테스트 침대")
                .discountPrice(10000L)
                .isExposed(true)
                .furnitureTagMappings(new HashSet<>())
                .build();

        Slice<CurationRawProduct> slice = new SliceImpl<>(
                List.of(product),
                PageRequest.of(0, size),
                false
        );

        given(curationRawProductRepository.findAllByCurationFilters(
                eq(keyword), any(), any(), any(), eq(cursor), any()
        )).willReturn(slice);

        // when
        CurationProductListResponse response = curationProductService.getProducts(
                keyword, typeIds, priceRangeIds, colorIds, cursor, size
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.products()).hasSize(1);
        assertThat(response.meta().appliedFilters()).hasSize(3);
    }

    @Test
    @DisplayName("getProductsV2()는 v2 레포지토리 메서드를 호출하여 토큰 기반 검색 결과를 반환한다")
    void getProductsV2() {
        // given
        String keyword = "매트리스";
        Integer size = 20;

        CurationRawProduct product = CurationRawProduct.builder()
                .id(99L)
                .productId(3003L)
                .productName("SS매트리스 침대")
                .discountPrice(200000L)
                .isExposed(true)
                .furnitureTagMappings(new HashSet<>())
                .build();
        product.updateSearchTokens("SS매트리스 침대 SS 매트리스");

        Slice<CurationRawProduct> slice = new SliceImpl<>(
                List.of(product),
                PageRequest.of(0, size),
                false
        );

        given(curationRawProductRepository.findAllByCurationFiltersV2(
                eq(keyword), any(), any(), any(), any(), any()
        )).willReturn(slice);

        // when
        CurationProductListResponse response = curationProductService.getProductsV2(
                keyword, null, null, null, null, size
        );

        // then
        assertThat(response.products()).hasSize(1);
        assertThat(response.products().get(0).name()).isEqualTo("SS매트리스 침대");
        assertThat(response.meta().hasNext()).isFalse();
    }

    @Test
    @DisplayName("getProductDetail()은 다중 매핑 시 우선순위가 가장 높은 카테고리명을 반환한다")
    void getProductDetail_withMultipleTags() {
        // given
        Long id = 1L;
        User user = User.builder().id(1L).build();

        FurnitureType bedType = FurnitureType.builder().nameEng("BED").build();
        Furniture bed = Furniture.builder().furnitureType(bedType).build();
        FurnitureTag bedTag = FurnitureTag.builder().furniture(bed).priority(5).build();

        FurnitureType sofaType = FurnitureType.builder().nameEng("SOFA").build();
        Furniture sofa = Furniture.builder().furnitureType(sofaType).build();
        FurnitureTag sofaTag = FurnitureTag.builder().furniture(sofa).priority(1).build();

        Set<CurationRawProductFurnitureTag> mappings = new HashSet<>();
        mappings.add(CurationRawProductFurnitureTag.builder().furnitureTag(bedTag).build());
        mappings.add(CurationRawProductFurnitureTag.builder().furnitureTag(sofaTag).build());

        CurationRawProduct product = CurationRawProduct.builder()
                .id(id)
                .productId(3003L)
                .isExposed(true)
                .furnitureTagMappings(mappings)
                .build();

        given(curationRawProductRepository.findByIdAndIsExposedTrueOrNull(id)).willReturn(Optional.of(product));
        given(curationRawProductColorRepository.findAllByCurationRawProductId(id)).willReturn(List.of());
        given(recommendFurnitureRepository.findBySourceAndFurnitureProductId(any(), any())).willReturn(Optional.empty());

        // when
        CurationProductDetailResponse response = curationProductService.getProductDetail(id, user);

        // then
        assertThat(response.product().categoryName()).isEqualTo("소파");
    }

    @Test
    @DisplayName("getProductDetail()은 RecommendFurniture가 존재하면 jjymCount를 함께 반환한다")
    void getProductDetail_withJjymCount() {
        // given
        Long id = 1L;
        User user = User.builder().id(1L).build();

        CurationRawProduct product = CurationRawProduct.builder()
                .id(id)
                .productId(3003L)
                .isExposed(true)
                .furnitureTagMappings(new HashSet<>())
                .build();

        RecommendFurniture recommendFurniture = RecommendFurniture.builder().id(10L).build();

        given(curationRawProductRepository.findByIdAndIsExposedTrueOrNull(id)).willReturn(Optional.of(product));
        given(curationRawProductColorRepository.findAllByCurationRawProductId(id)).willReturn(List.of());
        given(recommendFurnitureRepository.findBySourceAndFurnitureProductId(eq(CurationSource.RAW), eq(3003L)))
                .willReturn(Optional.of(recommendFurniture));
        given(jjymRepository.existsByUserIdAndRecommendFurnitureId(1L, 10L)).willReturn(true);
        given(jjymRepository.countByRecommendFurnitureId(10L)).willReturn(5L);

        // when
        CurationProductDetailResponse response = curationProductService.getProductDetail(id, user);

        // then
        assertThat(response.product().isLiked()).isTrue();
        assertThat(response.product().jjymCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getProductDetail()은 RecommendFurniture가 없으면 jjymCount를 0으로 반환한다")
    void getProductDetail_withNoRecommendFurniture() {
        // given
        Long id = 2L;

        CurationRawProduct product = CurationRawProduct.builder()
                .id(id)
                .productId(9999L)
                .isExposed(true)
                .furnitureTagMappings(new HashSet<>())
                .build();

        given(curationRawProductRepository.findByIdAndIsExposedTrueOrNull(id)).willReturn(Optional.of(product));
        given(curationRawProductColorRepository.findAllByCurationRawProductId(id)).willReturn(List.of());
        given(recommendFurnitureRepository.findBySourceAndFurnitureProductId(any(), any())).willReturn(Optional.empty());

        // when
        CurationProductDetailResponse response = curationProductService.getProductDetail(id, null);

        // then
        assertThat(response.product().isLiked()).isFalse();
        assertThat(response.product().jjymCount()).isEqualTo(0L);
    }
}
