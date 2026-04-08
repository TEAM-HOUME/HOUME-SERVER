package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.ActivityWithFurnitureResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.model.entity.ActivityFurniture;
import or.sopt.houme.domain.furniture.model.entity.Furniture;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTag;
import or.sopt.houme.domain.furniture.model.entity.FurnitureType;
import or.sopt.houme.domain.furniture.model.entity.FurnitureTypes;
import or.sopt.houme.domain.furniture.repository.ActivityFurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureRepository;
import or.sopt.houme.domain.furniture.repository.FurnitureTagRepository;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.furniture.repository.FurnitureTypeRepository;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.repository.taste.tag.TagRepository;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.global.api.handler.HouseException;
import or.sopt.houme.global.api.handler.TagException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("[Furniture Service] Test")
class FurnitureServiceImplTest {

    @InjectMocks
    FurnitureServiceImpl furnitureService;

    @Mock
    FurnitureRepository furnitureRepository;
    @Mock
    FurnitureTagRepository furnitureTagRepository;
    @Mock
    HouseRepository houseRepository;
    @Mock
    TagRepository tagRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("최융아")
                .email("user1@kakao.com")
                .build();
    }

    @Mock
    FurnitureTypeRepository furnitureTypeRepository;
    @Mock
    ActivityFurnitureRepository activityFurnitureRepository;

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

        // 가구 식별자
        Long furnitureId = 1L;
        // 2. 침대류
        List<Furniture> furnitureList = List.of(
                createFurniture(furnitureId++, "SINGLE", "싱글", bedType),
                createFurniture(furnitureId++, "SUPER_SINGLE", "슈퍼싱글", bedType),
                createFurniture(furnitureId++, "DOUBLE", "더블", bedType),
                createFurniture(furnitureId++, "QUEEN_OVER", "퀸 이상", bedType),
                createFurniture(furnitureId++, "ONE_SEATER_SOFA", "1인용 소파", sofaType),
                createFurniture(furnitureId++, "TWO_SEATER_SOFA", "2인용 소파", sofaType),
                createFurniture(furnitureId++, "CLOSET", "옷장", storageType),
                createFurniture(furnitureId++, "DRAWER", "서랍장", storageType),
                createFurniture(furnitureId++, "DESK", "업무용 책상", tableType),
                createFurniture(furnitureId++, "TABLE", "식탁", tableType),
                createFurniture(furnitureId++, "LOW_TABLE", "좌식 테이블", tableType),
                createFurniture(furnitureId++, "MOVABLE_TV", "이동식 TV", selectiveType),
                createFurniture(furnitureId++, "FULL_LENGTH_MIRROR", "전신 거울", selectiveType),
                createFurniture(furnitureId++, "BOOKSHELF", "책 선반", selectiveType),
                createFurniture(furnitureId++, "DECORATIVE_CABINET", "장식장", selectiveType)
        );

        when(furnitureTypeRepository.findAll()).thenReturn(categoryList);
        when(furnitureRepository.findAllWithFurnitureType()).thenReturn(furnitureList);

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

    @Test
    @DisplayName("감지된 단어와 선택 가구의 교집합만 추려 priority 오름차순으로 정렬된다")
    void categories_intersection_sorted() {
        // Given
        Long imageId = 10L;
        List<String> detectedObjects = List.of("SINGLE", "OFFICE_DESK", "Bed", "CLOSET", "DINING_TABLE", "BOX", "WHITE_BOOKSHELF");

        Tag tag = Tag.builder()
                .id(100L)
                .build();

        House house = House.builder()
                .id(200L)
                .build();

        // 이미지 생성 과정에서 사용자가 선택한 가구
        Furniture bed = Furniture.builder()
                .id(1L)
                .furnitureNameKr("침대")
                .furnitureNameEng("DOUBLE")
                .build();

        Furniture chair = Furniture.builder()
                .id(2L)
                .furnitureNameKr("의자")
                .furnitureNameEng("OFFICE_DESK")
                .build();

        Furniture tv = Furniture.builder()
                .id(3L)
                .furnitureNameKr("TV")
                .furnitureNameEng("Monitor/TV")
                .build();

        Furniture dining = Furniture.builder()
                .id(4L)
                .furnitureNameKr("식탁")
                .furnitureNameEng("DINING_TABLE")
                .build();

        // 레포지토리 Stubbing
        when(tagRepository.findTagByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.of(tag));
        when(houseRepository.findHouseByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.of(house));
        when(furnitureRepository.findAllByHouseId(house.getId()))
                .thenReturn(List.of(bed, chair, tv, dining));

        // furnitureTag 우선순위: Bed(4), Chair(3), TV(2), Dining Table(1)
        // 교집합은 Bed/Chair/Dining Table 이므로 그 3개만 반환
        FurnitureTag ftBed = FurnitureTag.builder()
                .id(11L).tag(tag).furniture(bed).priority(4).build();
        FurnitureTag ftChair = FurnitureTag.builder()
                .id(12L).tag(tag).furniture(chair).priority(3).build();
        FurnitureTag ftDining = FurnitureTag.builder()
                .id(13L).tag(tag).furniture(dining).priority(1).build();

        // 태그와 가구 리스트로 매핑 객체 조회
        when(furnitureTagRepository.findAllByTagIdAndFurnitureIn(tag.getId(), List.of(bed, chair, dining)))
                .thenReturn(List.of(ftBed, ftChair, ftDining));

        // When
        FurnitureCategoriesResponse response =
                furnitureService.getFurnitureCategoriesByStyle(user, imageId, detectedObjects);

        // Then
        // 교집합: Bed, Chair, Dining Table 만 포함
        // 정렬: priority 오름차순 → Dining Table(2) → Chair(4) → Bed(5)
        assertThat(response.categories()).hasSize(3);
        assertThat(response.categories())
                .extracting(FurnitureCategoriesResponse.FurnitureCategoryResponse::categoryName)
                .containsExactly("식탁", "의자", "침대");
    }

    @Test
    @DisplayName("주요활동별 매핑 가구를 조회할 수 있다.")
    void getActivityFurnitureMappings() {
        // Given
        FurnitureType tableType = FurnitureType.builder()
                .id(4L)
                .nameKr("테이블")
                .nameEng("TABLE")
                .build();

        FurnitureType selectiveType = FurnitureType.builder()
                .id(5L)
                .nameKr("그 외")
                .nameEng("SELECTIVE")
                .build();

        Furniture desk = createFurniture(10L, "DESK", "업무용 책상", tableType);
        Furniture bookshelf = createFurniture(11L, "BOOKSHELF", "책 선반", selectiveType);

        ActivityFurniture remoteWork = ActivityFurniture.builder()
                .id(1L)
                .activity(Activity.REMOTE_WORK)
                .furniture(desk)
                .priority(1)
                .build();

        ActivityFurniture reading = ActivityFurniture.builder()
                .id(2L)
                .activity(Activity.READING)
                .furniture(bookshelf)
                .priority(1)
                .build();

        when(activityFurnitureRepository.findAllByOrderByPriorityAscIdAsc())
                .thenReturn(List.of(remoteWork, reading));

        // When
        List<ActivityWithFurnitureResponse> responses = furnitureService.getActivityFurnitureMappings();

        // Then
        assertThat(responses).hasSize(Activity.values().length);
        assertThat(responses.get(0).code()).isEqualTo(Activity.REMOTE_WORK.name());
        assertThat(responses.get(0).furnitures())
                .extracting(furniture -> furniture.label())
                .containsExactly("업무용 책상");
        assertThat(responses.get(1).code()).isEqualTo(Activity.READING.name());
        assertThat(responses.get(1).furnitures())
                .extracting(furniture -> furniture.label())
                .containsExactly("책 선반");
    }

    @Test
    @DisplayName("Tag가 없을 경우 예외 발생")
    void getFurnitureCategoriesByStyle_tagNotFound() {
        // Given
        Long imageId = 10L;

        when(tagRepository.findTagByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(TagException.class,
                () -> furnitureService.getFurnitureCategoriesByStyle(user, imageId, List.of("Bed")));
    }

    @Test
    @DisplayName("House가 없을 경우 예외 발생")
    void getFurnitureCategoriesByStyle_houseNotFound() {
        // Given
        Long imageId = 10L;
        Tag tag = Tag.builder()
                .id(100L)
                .build();

        when(tagRepository.findTagByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.of(tag));
        when(houseRepository.findHouseByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(HouseException.class,
                () -> furnitureService.getFurnitureCategoriesByStyle(user, imageId, List.of("Bed")));
    }

    private Furniture createFurniture(Long id, String eng, String kr, FurnitureType type) {
        return Furniture.builder()
                .id(id)
                .furnitureNameEng(eng)
                .furnitureNameKr(kr)
                .furnitureType(type)
                .build();
    }

}
