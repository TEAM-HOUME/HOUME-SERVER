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
        assertThat(response.furnitureTypes()).hasSize(12);
        assertThat(response.furnitureTypes().get(1).nameKr()).isEqualTo("침대/프레임");
    }

    @Test
    @DisplayName("getProductDetail()은 상품 ID로 상세 정보를 조회하여 반환한다")
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
                .build();

        CurationRawProductColor color = CurationRawProductColor.builder()
                .curationRawProduct(product)
                .clientColorName("블랙")
                .build();

        given(curationRawProductRepository.findById(id)).willReturn(Optional.of(product));
        given(curationRawProductColorRepository.findAllByCurationRawProductId(id)).willReturn(List.of(color));
        // 찜 여부 관련 Mock 추가
        given(recommendFurnitureRepository.findBySourceAndFurnitureProductId(any(), eq(3003L))).willReturn(Optional.empty());

        // when
        CurationProductDetailResponse response = curationProductService.getProductDetail(id, user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.product().productId()).isEqualTo(3003L);
        assertThat(response.product().colors()).hasSize(1);
        assertThat(response.product().colors().get(0).name()).isEqualTo("블랙");
        assertThat(response.product().isLiked()).isFalse();
    }
}
