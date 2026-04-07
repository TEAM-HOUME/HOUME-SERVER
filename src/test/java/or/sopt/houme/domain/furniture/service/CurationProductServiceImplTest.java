package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.CurationRawProductColor;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
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

import java.util.List;
import java.util.Optional;

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
        Furniture furniture = Furniture.builder().id(5L).furnitureNameKr("업무용 책상").furnitureNameEng("OFFICE_DESK").build();

        given(furnitureTypeRepository.findAll()).willReturn(List.of(bedType));
        given(furnitureRepository.findAll()).willReturn(List.of(furniture));

        // when
        CurationProductFilterResponse response = curationProductService.getFilterMetadata();

        // then
        assertThat(response).isNotNull();
        
        // 1. 가구 유형 검증
        assertThat(response.furnitureTypes()).hasSize(12);
        assertThat(response.furnitureTypes().get(1).id()).isEqualTo(1L); // 침대/프레임
        assertThat(response.furnitureTypes().get(1).nameKr()).isEqualTo("침대/프레임");
        
        // 2. 가격대 검증
        assertThat(response.priceRanges()).hasSize(8);
        assertThat(response.priceRanges().get(1).min()).isEqualTo(0L);
        assertThat(response.priceRanges().get(2).min()).isEqualTo(50001L);
        
        // 3. 색상 검증
        assertThat(response.colors()).hasSize(15);
        assertThat(response.colors().get(0).label()).isEqualTo("화이트");
    }

    @Test
    @DisplayName("getProductDetail()은 노출된 상품만 ID로 상세 정보를 조회하여 반환한다")
    void getProductDetail() {
        // given
        Long id = 1L;
        User user = User.builder().id(1L).build();
        CurationRawProduct product = CurationRawProduct.builder()
                .id(id)
                .productId(3003L)
                .productName("테스트 상품")
                .productImageUrl("http://image.com")
                .productSiteUrl("http://site.com")
                .source("naver")
                .discountPrice(10000L)
                .isExposed(true)
                .build();

        CurationRawProductColor color = CurationRawProductColor.builder()
                .curationRawProduct(product)
                .clientColorName("블랙, 미매핑색상")
                .build();

        given(curationRawProductRepository.findByIdAndIsExposedTrue(id)).willReturn(Optional.of(product));
        given(curationRawProductColorRepository.findAllByCurationRawProductId(id)).willReturn(List.of(color));
        given(recommendFurnitureRepository.findBySourceAndFurnitureProductId(any(), eq(3003L))).willReturn(Optional.empty());

        // when
        CurationProductDetailResponse response = curationProductService.getProductDetail(id, user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.product().productId()).isEqualTo(3003L);
        assertThat(response.product().colors()).hasSize(2);
        
        // 블랙 매핑 확인
        assertThat(response.product().colors().get(0).name()).isEqualTo("블랙");
        assertThat(response.product().colors().get(0).value()).isEqualTo("#000000");
        
        // 미매핑 색상 확인
        assertThat(response.product().colors().get(1).name()).isEqualTo("미매핑색상");
        assertThat(response.product().colors().get(1).value()).isNull();
        
        assertThat(response.product().isLiked()).isFalse();
    }
}
