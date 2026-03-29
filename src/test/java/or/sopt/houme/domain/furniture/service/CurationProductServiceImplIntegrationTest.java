package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.presentation.dto.response.CurationProductFilterResponse;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("상품 큐레이션 서비스 통합 테스트")
class CurationProductServiceImplIntegrationTest {

    @Autowired
    private CurationProductService curationProductService;

    @Autowired
    private FurnitureTypeRepository furnitureTypeRepository;

    @Autowired
    private FurnitureRepository furnitureRepository;

    @BeforeEach
    void setUp() {
        // 필수 대분류(FurnitureType) 생성
        FurnitureType bedType = furnitureTypeRepository.save(FurnitureType.builder()
                .nameKr("침대")
                .nameEng("BED")
                .isRequired(true)
                .build());

        FurnitureType storageType = furnitureTypeRepository.save(FurnitureType.builder()
                .nameKr("수납")
                .nameEng("STORAGE")
                .isRequired(false)
                .build());

        // 필수 중분류(Furniture) 생성
        furnitureRepository.save(Furniture.builder()
                .furnitureNameKr("업무용 책상")
                .furnitureNameEng("OFFICE_DESK")
                .furnitureType(bedType) // 예시 연관 관계
                .build());
    }

    @Test
    @DisplayName("getFilterMetadata()가 DB 데이터와 정적 필터를 조합하여 반환한다")
    void getFilterMetadata() {
        // when
        CurationProductFilterResponse response = curationProductService.getFilterMetadata();

        // then
        assertThat(response).isNotNull();
        
        // 검증: 침대/프레임(BED)이 DB에서 조회되어 포함되어 있는지
        boolean hasBed = response.furnitureTypes().stream()
                .anyMatch(ft -> ft.nameKr().equals("침대/프레임") && ft.nameEng().equals("BED"));
        assertThat(hasBed).isTrue();

        // 검증: 가격대 P1(5만원 이하) 정적 데이터가 포함되어 있는지
        boolean hasP1 = response.priceRanges().stream()
                .anyMatch(pr -> pr.id().equals("P1") && pr.min().equals(0L));
        assertThat(hasP1).isTrue();
    }
}
