package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.FurnitureType;
import or.sopt.houme.domain.furniture.entity.FurnitureTypes;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.house.entity.enums.Activity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("[Furniture Service] Test")
class FurnitureServiceImplTest {

    @InjectMocks
    FurnitureServiceImpl furnitureService;

    @Mock
    FurnitureRepository furnitureRepository;

    @Mock
    FurnitureTypeRepository furnitureTypeRepository;

    @Test
    @DisplayName("주요활동, 가구들에 대한 정보들을 받을 수 있다.")
    void getFurniture() {
        // Given
        // 1. FurnitureType 저장
        // 침대
        FurnitureType bedType = FurnitureType.builder()
                .id(1L)
                .nameKr("침대")
                .nameEng("BED")
                .isRequired(true)
                .build();

        // 소파
        FurnitureType sofaType = FurnitureType.builder()
                .id(2L)
                .nameKr("소파")
                .nameEng("SOFA")
                .build();

        // 수납
        FurnitureType storageType = FurnitureType.builder()
                .id(3L)
                .nameKr("수납")
                .nameEng("STORAGE")
                .build();

        // 테이블
        FurnitureType tableType = FurnitureType.builder()
                .id(4L)
                .nameKr("테이블")
                .nameEng("TABLE")
                .build();

        // 그외
        FurnitureType selectiveType = FurnitureType.builder()
                .id(5L)
                .nameKr("그 외")
                .nameEng("SELECTIVE")
                .isRequired(false)
                .build();

        List<FurnitureType> categoryList = List.of(bedType, sofaType, storageType, tableType, selectiveType);

        // 2. 침대류
        List<Furniture> furnitureList = List.of(
                createFurniture("SINGLE", "싱글", bedType),
                createFurniture("SUPER_SINGLE", "슈퍼싱글", bedType),
                createFurniture("DOUBLE", "더블", bedType),
                createFurniture("QUEEN_OVER", "퀸 이상", bedType),
                createFurniture("ONE_SEATER_SOFA", "1인용 소파", sofaType),
                createFurniture("TWO_SEATER_SOFA", "2인용 소파", sofaType),
                createFurniture("CLOSET", "옷장", storageType),
                createFurniture("DRAWER", "서랍장", storageType),
                createFurniture("DESK", "업무용 책상", tableType),
                createFurniture("TABLE", "식탁", tableType),
                createFurniture("LOW_TABLE", "좌식 테이블", tableType),
                createFurniture("MOVABLE_TV", "이동식 TV", selectiveType),
                createFurniture("FULL_LENGTH_MIRROR", "전신 거울", selectiveType),
                createFurniture("BOOKSHELF", "책 선반", selectiveType),
                createFurniture("DECORATIVE_CABINET", "장식장", selectiveType)
        );

        when(furnitureTypeRepository.findAll()).thenReturn(categoryList);
        when(furnitureRepository.findAll()).thenReturn(furnitureList);

        // When
        FurnitureAndActivityResponse furnitureAndActivity = furnitureService.getFurnitureAndActivity();

        // Then
        assertThat(furnitureAndActivity).isNotNull();
        assertThat(furnitureAndActivity.activities().get(0).code()).isEqualTo(Activity.REMOTE_WORK.toString());
        assertThat(furnitureAndActivity.categories().get(0).nameKr()).isEqualTo("침대");
        assertThat(furnitureAndActivity.categories().get(1).nameKr()).isEqualTo("소파");
        assertThat(furnitureAndActivity.categories().get(2).nameKr()).isEqualTo("수납");
        assertThat(furnitureAndActivity.categories().get(3).nameKr()).isEqualTo("테이블");
        assertThat(furnitureAndActivity.categories().get(4).nameKr()).isEqualTo("그 외");
    }

    private Furniture createFurniture(String eng, String kr, FurnitureType type) {
        return Furniture.builder()
                .furnitureNameEng(eng)
                .furnitureNameKr(kr)
                .furnitureType(type)
                .build();
    }
}