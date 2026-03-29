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
    @DisplayName("getFilterMetadata()는 DB 데이터를 기반으로 하이브리드 필터 목록을 생성한다")
    void getFilterMetadataSuccess() {
        // given
        FurnitureType bedType = FurnitureType.builder().id(1L).nameEng("BED").nameKr("침대").build();
        Furniture officeDesk = Furniture.builder().id(5L).furnitureNameEng("OFFICE_DESK").furnitureNameKr("업무용 책상").build();

        given(furnitureTypeRepository.findAll()).willReturn(List.of(bedType));
        given(furnitureRepository.findAll()).willReturn(List.of(officeDesk));

        // when
        CurationProductFilterResponse response = curationProductService.getFilterMetadata();

        // then
        assertThat(response.furnitureTypes()).hasSize(12); // 기획된 12개 항목 확인
        
        // 하이브리드 매핑 검증 (대분류)
        assertThat(response.furnitureTypes().get(1).id()).isEqualTo(1L);
        assertThat(response.furnitureTypes().get(1).nameKr()).isEqualTo("침대/프레임");

        // 하이브리드 매핑 검증 (중분류)
        assertThat(response.furnitureTypes().get(2).id()).isEqualTo(5L);
        assertThat(response.furnitureTypes().get(2).nameKr()).isEqualTo("업무용 책상");

        // 가격대(8종) 및 색상(9종) 검증
        assertThat(response.priceRanges()).hasSize(8);
        assertThat(response.colors()).hasSize(9);
    }
}
