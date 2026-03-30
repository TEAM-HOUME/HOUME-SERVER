package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.model.entity.CurationRawProduct;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductDetailResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.repository.CurationRawProductRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(response.priceRanges()).hasSize(8);
        assertThat(response.colors()).hasSize(15);
    }

    @Test
    @DisplayName("getProductDetail()은 상품 ID로 상세 정보를 조회하여 반환한다")
    void getProductDetail() {
        // given
        Long id = 1L;
        CurationRawProduct product = CurationRawProduct.builder()
                .id(id)
                .productId(3003L)
                .productName("테스트 상품")
                .productImageUrl("http://image.com")
                .productSiteUrl("http://site.com")
                .source("naver")
                .discountPrice(10000L)
                .build();

        given(curationRawProductRepository.findById(id)).willReturn(Optional.of(product));

        // when
        CurationProductDetailResponse response = curationProductService.getProductDetail(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.product().productId()).isEqualTo(3003L);
        assertThat(response.product().name()).isEqualTo("테스트 상품");
        assertThat(response.product().categoryName()).isEqualTo("기타");
    }
}
