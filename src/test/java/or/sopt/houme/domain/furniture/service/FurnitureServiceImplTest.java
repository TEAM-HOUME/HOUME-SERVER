package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.entity.Furniture;
import or.sopt.houme.domain.furniture.entity.FurnitureType;
import or.sopt.houme.domain.furniture.entity.FurnitureTypes;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
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

    @Test
    @DisplayName("주요활동, 가구들에 대한 정보들을 받을 수 있다.")
    void getFurniture() {
        // Given
        // 1. FurnitureType 저장
        FurnitureType bedType = FurnitureType.builder()
                .furnitureType(FurnitureTypes.BED)
                .isRequired(true)
                .build();

        FurnitureType selectiveType = FurnitureType.builder()
                .furnitureType(FurnitureTypes.SELECTIVE)
                .isRequired(false)
                .build();

        // 2. 침대류
        List<Furniture> beds = List.of(
                createFurniture("SINGLE", "싱글", bedType),
                createFurniture("SUPER_SINGLE", "슈퍼싱글", bedType),
                createFurniture("DOUBLE", "더블", bedType),
                createFurniture("QUEEN_OVER", "퀸 이상", bedType),
                createFurniture("DESK", "책상", selectiveType),
                createFurniture("MOVABLE_TV", "이동식 TV", selectiveType),
                createFurniture("DRAWER", "서랍장", selectiveType),
                createFurniture("TABLE_CHAIRS", "식탁, 의자", selectiveType),
                createFurniture("CLOSET", "옷장", selectiveType),
                createFurniture("SOFA", "소파", selectiveType)
        );

        when(furnitureRepository.findAll()).thenReturn(beds);

        // When
        FurnitureAndActivityResponse furnitureAndActivity = furnitureService.getFurnitureAndActivity();

        // Then
        assertThat(furnitureAndActivity).isNotNull();
        assertThat(furnitureAndActivity.activities().get(0).code()).isEqualTo(Activity.RELAXING.toString());
        assertThat(furnitureAndActivity.beds().isRequired()).isTrue();
        assertThat(furnitureAndActivity.selectives().isRequired()).isFalse();
        assertThat(furnitureAndActivity.beds().items()).isNotEmpty();
        assertThat(furnitureAndActivity.selectives().items()).isNotEmpty();
    }

    private Furniture createFurniture(String eng, String kr, FurnitureType type) {
        return Furniture.builder()
                .furnitureNameEng(eng)
                .furnitureNameKr(kr)
                .furnitureType(type)
                .build();
    }
}