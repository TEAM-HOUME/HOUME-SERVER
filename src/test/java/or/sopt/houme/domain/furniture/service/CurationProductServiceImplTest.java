package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
        assertThat(response.furnitureTypes().get(2).id()).isEqualTo(5L); // 업무용 책상
        
        // 2. 가격대 검증 (경계값 중복 제거 확인)
        assertThat(response.priceRanges()).hasSize(8);
        assertThat(response.priceRanges().get(1).min()).isEqualTo(0L);
        assertThat(response.priceRanges().get(1).max()).isEqualTo(50000L);
        assertThat(response.priceRanges().get(2).min()).isEqualTo(50001L); // 50001부터 시작하는지 확인
        
        // 3. 색상 검증 (15종 확인)
        assertThat(response.colors()).hasSize(15);
        assertThat(response.colors().get(0).label()).isEqualTo("화이트");
        assertThat(response.colors().get(0).id()).isEqualTo(1L);
    }
}
