package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductFurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    @DisplayName("getProductDetail()은 다중 매핑 시 우선순위가 가장 높은 카테고리명을 반환한다")
    void getProductDetail_withMultipleTags() {
        // given
        Long id = 1L;
        User user = User.builder().id(1L).build();

        // 우선순위 5인 침대 태그
        FurnitureType bedType = FurnitureType.builder().nameEng("BED").build();
        Furniture bed = Furniture.builder().furnitureType(bedType).build();
        FurnitureTag bedTag = FurnitureTag.builder().furniture(bed).priority(5).build();

        // 우선순위 1인 소파 태그 (더 높음)
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

        given(curationRawProductRepository.findByIdAndIsExposedTrue(id)).willReturn(Optional.of(product));
        given(curationRawProductColorRepository.findAllByCurationRawProductId(id)).willReturn(List.of());
        given(recommendFurnitureRepository.findBySourceAndFurnitureProductId(any(), any())).willReturn(Optional.empty());

        // when
        CurationProductDetailResponse response = curationProductService.getProductDetail(id, user);

        // then
        // 우선순위 1인 소파가 선택되어야 함
        assertThat(response.product().categoryName()).isEqualTo("소파");
    }
}
