package or.sopt.houme.domain.furniture.service;

import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureAndActivityResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.ActivityWithFurnitureResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoriesResponse;
import or.sopt.houme.domain.furniture.presentation.dto.response.FurnitureCategoryGroup;
import or.sopt.houme.furniture.domain.ActivityFurnitureView;
import or.sopt.houme.furniture.domain.Furniture;
import or.sopt.houme.furniture.domain.FurnitureTagView;
import or.sopt.houme.furniture.domain.FurnitureTypeView;
import or.sopt.houme.furniture.domain.FurnitureWithTypeView;
import or.sopt.houme.furniture.domain.port.out.ActivityFurnitureQueryPort;
import or.sopt.houme.furniture.domain.port.out.FurnitureRepositoryPort;
import or.sopt.houme.furniture.domain.port.out.FurnitureTagQueryPort;
import or.sopt.houme.furniture.domain.port.out.FurnitureTypeQueryPort;
import or.sopt.houme.house.domain.port.out.HouseQueryPort;
import or.sopt.houme.domain.house.model.entity.enums.Activity;
import or.sopt.houme.tag.domain.Tag;
import or.sopt.houme.tag.domain.port.out.TagRepositoryPort;
import or.sopt.houme.user.domain.User;
import or.sopt.houme.global.api.handler.HouseException;
import or.sopt.houme.global.api.handler.TagException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
    FurnitureRepositoryPort furnitureRepositoryPort;
    @Mock
    FurnitureTagQueryPort furnitureTagQueryPort;
    @Mock
    HouseQueryPort houseQueryPort;
    @Mock
    TagRepositoryPort tagRepository;

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
    FurnitureTypeQueryPort furnitureTypeQueryPort;
    @Mock
    ActivityFurnitureQueryPort activityFurnitureQueryPort;
    @Mock
    CurationRawProductFurnitureService curationRawProductFurnitureService;

    private static FurnitureTypeView type(Long id, String kr, String eng, Boolean required, Integer priority) {
        return new FurnitureTypeView(id, kr, eng, required, priority);
    }

    private static FurnitureWithTypeView furnitureView(Long id, String eng, String kr, FurnitureTypeView type) {
        return new FurnitureWithTypeView(id, eng, kr, null, type.id(), type.nameKr(), type.nameEng());
    }

    private static Furniture furniture(Long id, String eng, String kr) {
        return Furniture.reconstitute(id, eng, kr, null, null, null);
    }

    private static FurnitureTagView tagView(Long id, Long tagId, Furniture furniture, Integer priority) {
        return new FurnitureTagView(id, null, furniture.getId(), furniture.getFurnitureNameKr(), tagId, null, null, priority);
    }

    @Test
    @DisplayName("주요활동, 가구들에 대한 정보들을 받을 수 있다.")
    void getFurniture() {
        FurnitureTypeView bedType = type(1L, "침대", "BED", true, null);
        FurnitureTypeView sofaType = type(2L, "소파", "SOFA", null, null);
        FurnitureTypeView storageType = type(3L, "수납", "STORAGE", null, null);
        FurnitureTypeView tableType = type(4L, "테이블", "TABLE", null, null);
        FurnitureTypeView selectiveType = type(5L, "그 외", "SELECTIVE", false, null);

        List<FurnitureTypeView> categoryList = List.of(bedType, sofaType, storageType, tableType, selectiveType);

        long id = 1L;
        List<FurnitureWithTypeView> furnitureList = List.of(
                furnitureView(id++, "SINGLE", "싱글", bedType),
                furnitureView(id++, "SUPER_SINGLE", "슈퍼싱글", bedType),
                furnitureView(id++, "DOUBLE", "더블", bedType),
                furnitureView(id++, "QUEEN_OVER", "퀸 이상", bedType),
                furnitureView(id++, "ONE_SEATER_SOFA", "1인용 소파", sofaType),
                furnitureView(id++, "TWO_SEATER_SOFA", "2인용 소파", sofaType),
                furnitureView(id++, "CLOSET", "옷장", storageType),
                furnitureView(id++, "DRAWER", "서랍장", storageType),
                furnitureView(id++, "DESK", "업무용 책상", tableType),
                furnitureView(id++, "TABLE", "식탁", tableType),
                furnitureView(id++, "LOW_TABLE", "좌식 테이블", tableType),
                furnitureView(id++, "MOVABLE_TV", "이동식 TV", selectiveType),
                furnitureView(id++, "FULL_LENGTH_MIRROR", "전신 거울", selectiveType),
                furnitureView(id++, "BOOKSHELF", "책 선반", selectiveType),
                furnitureView(id++, "DECORATIVE_CABINET", "장식장", selectiveType)
        );

        when(furnitureTypeQueryPort.findAll()).thenReturn(categoryList);
        when(furnitureRepositoryPort.findAllWithType()).thenReturn(furnitureList);

        FurnitureAndActivityResponse furnitureAndActivity = furnitureService.getFurnitureAndActivity();

        assertThat(furnitureAndActivity).isNotNull();
        assertThat(furnitureAndActivity.activities().get(0).code()).isEqualTo(Activity.REMOTE_WORK.toString());
        assertThat(furnitureAndActivity.categories().get(0).nameKr()).isEqualTo("침대");
        assertThat(furnitureAndActivity.categories().get(1).nameKr()).isEqualTo("소파");
        assertThat(furnitureAndActivity.categories().get(2).nameKr()).isEqualTo("수납");
        assertThat(furnitureAndActivity.categories().get(3).nameKr()).isEqualTo("테이블");
        assertThat(furnitureAndActivity.categories().get(4).nameKr()).isEqualTo("그 외");
    }

    @Test
    @DisplayName("대시보드 카테고리만 별도로 조회할 수 있다.")
    void getDashboardCategories() {
        FurnitureTypeView bedType = type(1L, "침대", "BED", null, null);
        FurnitureTypeView sofaType = type(2L, "소파", "SOFA", null, null);

        when(furnitureTypeQueryPort.findAll()).thenReturn(List.of(bedType, sofaType));
        when(furnitureRepositoryPort.findAllWithType()).thenReturn(List.of(
                furnitureView(10L, "SINGLE", "싱글", bedType),
                furnitureView(20L, "SINGLE_SOFA", "1인용 소파", sofaType)
        ));

        List<FurnitureCategoryGroup> categories = furnitureService.getDashboardCategories();

        assertThat(categories).hasSize(2);
        assertThat(categories.get(0).nameKr()).isEqualTo("침대");
        assertThat(categories.get(1).nameKr()).isEqualTo("소파");
    }

    @Test
    @DisplayName("대시보드 카테고리는 furnitureType priority 오름차순으로 정렬된다.")
    void getDashboardCategories_sortedByFurnitureTypePriority() {
        FurnitureTypeView sofaType = type(10L, "소파", "SOFA", null, 2);
        FurnitureTypeView bedType = type(20L, "침대/프레임", "BED", null, 1);

        when(furnitureTypeQueryPort.findAll()).thenReturn(List.of(sofaType, bedType));
        when(furnitureRepositoryPort.findAllWithType()).thenReturn(List.of(
                furnitureView(1L, "SINGLE_SOFA", "1인용 소파", sofaType),
                furnitureView(2L, "SINGLE", "싱글", bedType)
        ));

        List<FurnitureCategoryGroup> categories = furnitureService.getDashboardCategories();

        assertThat(categories).extracting(FurnitureCategoryGroup::nameKr)
                .containsExactly("침대/프레임", "소파");
    }

    @Test
    @DisplayName("감지된 단어와 선택 가구의 교집합만 추려 priority 오름차순으로 정렬된다")
    void categories_intersection_sorted() {
        Long imageId = 10L;
        List<String> detectedObjects = List.of("SINGLE", "OFFICE_DESK", "Bed", "CLOSET", "DINING_TABLE", "BOX", "WHITE_BOOKSHELF");

        Tag tag = Tag.builder().id(100L).build();

        Furniture bed = furniture(1L, "DOUBLE", "침대");
        Furniture chair = furniture(2L, "OFFICE_DESK", "의자");
        Furniture tv = furniture(3L, "Monitor/TV", "TV");
        Furniture dining = furniture(4L, "DINING_TABLE", "식탁");

        when(tagRepository.findTagByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.of(tag));
        when(houseQueryPort.findHouseIdByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.of(200L));
        when(furnitureRepositoryPort.findAllByHouseId(200L))
                .thenReturn(List.of(bed, chair, tv, dining));

        FurnitureTagView ftBed = tagView(11L, 100L, bed, 4);
        FurnitureTagView ftChair = tagView(12L, 100L, chair, 3);
        FurnitureTagView ftDining = tagView(13L, 100L, dining, 1);

        // 교집합(detectedObjects 확장 매칭): DOUBLE(single 확장), OFFICE_DESK, DINING_TABLE
        when(furnitureTagQueryPort.findAllByTagIdAndFurnitureIdIn(tag.getId(), List.of(1L, 2L, 4L)))
                .thenReturn(List.of(ftBed, ftChair, ftDining));

        FurnitureCategoriesResponse response =
                furnitureService.getFurnitureCategoriesByStyle(user, imageId, detectedObjects);

        assertThat(response.categories()).hasSize(3);
        assertThat(response.categories())
                .extracting(FurnitureCategoriesResponse.FurnitureCategoryResponse::categoryName)
                .containsExactly("식탁", "의자", "침대");
    }

    @Test
    @DisplayName("주요활동별 매핑 가구를 조회할 수 있다.")
    void getActivityFurnitureMappings() {
        FurnitureTypeView tableType = type(4L, "테이블", "TABLE", null, null);
        FurnitureTypeView selectiveType = type(5L, "그 외", "SELECTIVE", null, null);

        FurnitureWithTypeView desk = furnitureView(10L, "DESK", "업무용 책상", tableType);
        FurnitureWithTypeView bookshelf = furnitureView(11L, "BOOKSHELF", "책 선반", selectiveType);

        when(activityFurnitureQueryPort.findAllOrderByPriorityAscIdAsc())
                .thenReturn(List.of(
                        new ActivityFurnitureView(Activity.REMOTE_WORK, 1, desk),
                        new ActivityFurnitureView(Activity.READING, 1, bookshelf)
                ));

        List<ActivityWithFurnitureResponse> responses = furnitureService.getActivityFurnitureMappings();

        assertThat(responses).hasSize(2);
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
        Long imageId = 10L;

        when(tagRepository.findTagByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.empty());

        assertThrows(TagException.class,
                () -> furnitureService.getFurnitureCategoriesByStyle(user, imageId, List.of("Bed")));
    }

    @Test
    @DisplayName("House가 없을 경우 예외 발생")
    void getFurnitureCategoriesByStyle_houseNotFound() {
        Long imageId = 10L;
        Tag tag = Tag.builder().id(100L).build();

        when(tagRepository.findTagByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.of(tag));
        when(houseQueryPort.findHouseIdByUserIdAndImageId(user.getId(), imageId))
                .thenReturn(Optional.empty());

        assertThrows(HouseException.class,
                () -> furnitureService.getFurnitureCategoriesByStyle(user, imageId, List.of("Bed")));
    }

    @Test
    @DisplayName("V2 카테고리 조회는 detectedObjects 없이 선택 가구 기준으로 정렬 응답한다")
    void getFurnitureCategoriesByStyleV2_fromSelectedFurnitures_sorted() {
        Long imageId = 10L;

        Tag tag = Tag.builder().id(100L).build();

        Furniture bed = furniture(1L, null, "침대");
        Furniture chair = furniture(2L, null, "의자");
        Furniture tv = furniture(3L, null, "TV");
        Furniture dining = furniture(4L, null, "식탁");

        FurnitureTagView ftBed = tagView(11L, 100L, bed, 4);
        FurnitureTagView ftChair = tagView(12L, 100L, chair, 3);
        FurnitureTagView ftTv = tagView(14L, 100L, tv, 2);
        FurnitureTagView ftDining = tagView(13L, 100L, dining, 1);

        when(tagRepository.findTagByUserIdAndImageId(user.getId(), imageId)).thenReturn(Optional.of(tag));
        when(houseQueryPort.findHouseIdByUserIdAndImageId(user.getId(), imageId)).thenReturn(Optional.of(200L));
        when(furnitureRepositoryPort.findAllByHouseId(200L)).thenReturn(List.of(bed, chair, tv, dining));
        when(furnitureTagQueryPort.findAllByTagIdAndFurnitureIdIn(tag.getId(), List.of(1L, 2L, 3L, 4L)))
                .thenReturn(List.of(ftBed, ftChair, ftTv, ftDining));
        when(curationRawProductFurnitureService.getFurnitureIdsHavingProducts(List.of(1L, 2L, 3L, 4L)))
                .thenReturn(List.of());

        FurnitureCategoriesResponse response = furnitureService.getFurnitureCategoriesByStyleV2(user, imageId);

        assertThat(response.categories())
                .extracting(FurnitureCategoriesResponse.FurnitureCategoryResponse::categoryName)
                .containsExactly("식탁", "TV", "의자", "침대");
    }

    @Test
    @DisplayName("V2 카테고리 조회 - FurnitureTag 없는 가구도 CurationRawProductFurniture 매핑 있으면 카테고리에 포함된다")
    void getFurnitureCategoriesByStyleV2_includesExtraFromCurationRawProductFurniture() {
        Long imageId = 10L;

        Tag tag = Tag.builder().id(100L).build();

        Furniture sofa = furniture(1L, null, "소파");
        Furniture desk = furniture(2L, null, "책상");

        FurnitureTagView ftSofa = tagView(11L, 100L, sofa, 1);

        when(tagRepository.findTagByUserIdAndImageId(user.getId(), imageId)).thenReturn(Optional.of(tag));
        when(houseQueryPort.findHouseIdByUserIdAndImageId(user.getId(), imageId)).thenReturn(Optional.of(200L));
        when(furnitureRepositoryPort.findAllByHouseId(200L)).thenReturn(List.of(sofa, desk));
        when(furnitureTagQueryPort.findAllByTagIdAndFurnitureIdIn(tag.getId(), List.of(1L, 2L)))
                .thenReturn(List.of(ftSofa));
        when(curationRawProductFurnitureService.getFurnitureIdsHavingProducts(List.of(1L, 2L)))
                .thenReturn(List.of(2L));
        when(furnitureRepositoryPort.findAllById(List.of(2L))).thenReturn(List.of(desk));

        FurnitureCategoriesResponse response = furnitureService.getFurnitureCategoriesByStyleV2(user, imageId);

        assertThat(response.categories()).hasSize(2);
        assertThat(response.categories())
                .extracting(FurnitureCategoriesResponse.FurnitureCategoryResponse::categoryName)
                .containsExactly("소파", "책상");
    }

    @Test
    @DisplayName("V2 카테고리 조회 - FurnitureTag와 CurationRawProductFurniture에 모두 있는 가구는 중복 없이 한 번만 반환된다")
    void getFurnitureCategoriesByStyleV2_noDuplicateWhenBothPathsExist() {
        Long imageId = 10L;

        Tag tag = Tag.builder().id(100L).build();

        Furniture bed = furniture(1L, null, "침대");

        FurnitureTagView ftBed = tagView(11L, 100L, bed, 1);

        when(tagRepository.findTagByUserIdAndImageId(user.getId(), imageId)).thenReturn(Optional.of(tag));
        when(houseQueryPort.findHouseIdByUserIdAndImageId(user.getId(), imageId)).thenReturn(Optional.of(200L));
        when(furnitureRepositoryPort.findAllByHouseId(200L)).thenReturn(List.of(bed));
        when(furnitureTagQueryPort.findAllByTagIdAndFurnitureIdIn(tag.getId(), List.of(1L)))
                .thenReturn(List.of(ftBed));
        when(curationRawProductFurnitureService.getFurnitureIdsHavingProducts(List.of(1L)))
                .thenReturn(List.of(1L));

        FurnitureCategoriesResponse response = furnitureService.getFurnitureCategoriesByStyleV2(user, imageId);

        assertThat(response.categories()).hasSize(1);
        assertThat(response.categories().get(0).categoryName()).isEqualTo("침대");
    }
}
