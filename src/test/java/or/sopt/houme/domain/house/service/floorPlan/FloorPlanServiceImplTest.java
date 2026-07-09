package or.sopt.houme.domain.house.service.floorPlan;

import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.mapping.HouseFloorPlan;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateDetailResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.RecentFloorPlanResponse;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.model.floorPlan.vo.FloorPlanImageItem;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanEquilibriumJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanFormJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImageJsonCodec;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanStructureJsonCodec;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("[FloorPlan Service Test]")
class FloorPlanServiceImplTest {

    @InjectMocks
    FloorPlanServiceImpl floorPlanService;

    @Mock
    FloorPlanRepository floorPlanRepository;

    @Mock
    GenerateImageRepository generateImageRepository;

    @Mock
    FloorPlanImageJsonCodec floorPlanImageJsonCodec;

    @Mock
    FloorPlanFormJsonCodec floorPlanFormJsonCodec;

    @Mock
    FloorPlanStructureJsonCodec floorPlanStructureJsonCodec;

    @Mock
    FloorPlanEquilibriumJsonCodec floorPlanEquilibriumJsonCodec;

    @Test
    @DisplayName("최근 생성 이력이 없으면 floorPlan은 null 필드 객체로 반환한다")
    void getRecentFloorPlan_returnsEmptyObjectWhenNoRecentImage() {
        RecentFloorPlanResponse response = floorPlanService.getRecentFloorPlan(null);

        assertThat(response.hasRecentImage()).isFalse();
        assertThat(response.floorPlanId()).isNull();
        assertThat(response.floorPlanName()).isNull();
        assertThat(response.equilibrium()).isNull();
        assertThat(response.floorPlans()).isEmpty();
    }

    @Test
    @DisplayName("최근 생성 도면 조회 시 가장 최근 도면의 모든 view 이미지를 배열로 반환한다")
    void getRecentFloorPlan_returnsAllImages() {
        User user = User.builder()
                .id(1L)
                .build();
        FloorPlan floorPlan = FloorPlan.builder()
                .id(10L)
                .floorPlanName("복층 오피스텔")
                .equilibrium(Equilibrium.BETWEEN_6_10)
                .imagesJson("images-json")
                .url("https://fallback-image")
                .filename("fallback.png")
                .originalFilename("fallback-origin.png")
                .fileExtension("png")
                .build();
        HouseFloorPlan houseFloorPlan = HouseFloorPlan.builder()
                .id(100L)
                .floorPlan(floorPlan)
                .selectedView("SIDE_VIEW")
                .build();
        House house = House.builder()
                .houseFloorPlans(List.of(houseFloorPlan))
                .build();
        GenerateImage generateImage = GenerateImage.builder()
                .house(house)
                .build();

        when(generateImageRepository.findMostRecentByUserId(1L)).thenReturn(Optional.of(generateImage));
        when(floorPlanImageJsonCodec.read("images-json")).thenReturn(List.of(
                FloorPlanImageItem.create("https://image-1", "file-1.png", "origin-1.png", "png", 1, "TOP_VIEW"),
                FloorPlanImageItem.create("https://image-2", "file-2.png", "origin-2.png", "png", 2, "SIDE_VIEW")
        ));

        RecentFloorPlanResponse response = floorPlanService.getRecentFloorPlan(user);

        assertThat(response.hasRecentImage()).isTrue();
        assertThat(response.floorPlanId()).isEqualTo(10L);
        assertThat(response.floorPlanName()).isEqualTo("복층 오피스텔");
        assertThat(response.equilibrium()).isEqualTo(Equilibrium.BETWEEN_6_10.getDescription());
        assertThat(response.floorPlans())
                .extracting("imageUrl", "view", "isRecentUsedView")
                .containsExactly(
                        tuple("https://image-1", "TOP_VIEW", false),
                        tuple("https://image-2", "SIDE_VIEW", true)
                );
    }

    @Test
    @DisplayName("사용자가 입력한 값들을 토대로 도면 템플릿을 필터링해서 내려줄 수 있다.")
    void getHousingPlan() {
        // Given
        Form officetel = Form.OFFICETEL;
        Structure openOneRoom = Structure.OPEN_ONE_ROOM;
        Equilibrium under5 = Equilibrium.UNDER_5;
        String url = "imageUrl";
        String filename = "filename";
        String originalFilename = "originalFilename";
        String fileExtension = "jps";

        FloorPlan floorPlan1 = FloorPlan.builder()
                .id(1L)
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .floorPlanPrompt("prompt1")
                .url(url)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(fileExtension)
                .build();
        FloorPlan floorPlan2 = FloorPlan.builder()
                .id(2L)
                .form(Form.ETC)
                .structure(Structure.DUPLEX)
                .floorPlanPrompt("prompt2")
                .url(url)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(fileExtension)
                .build();
        FloorPlan floorPlan3 = FloorPlan.builder()
                .id(3L)
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .floorPlanPrompt("prompt3")
                .url(url)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(fileExtension)
                .build();

        when(floorPlanRepository.findAll()).thenReturn(List.of(floorPlan1, floorPlan2, floorPlan3));
        when(floorPlanFormJsonCodec.read(null)).thenReturn(List.of());
        when(floorPlanStructureJsonCodec.read(null)).thenReturn(List.of());

        // When
        FloorPlanListResponse housingPlan = floorPlanService.getHousingPlan(officetel, openOneRoom);

        // Then
        assertThat(housingPlan).isNotNull();
        assertThat(housingPlan.floorPlanList().size()).isEqualTo(2);
        assertThat(housingPlan.floorPlanList().get(0))
                .extracting("id", "form", "structure", "floorPlanImage")
                .contains(1L, officetel, openOneRoom, url);
        assertThat(housingPlan.floorPlanList()).hasSize(2)
                .extracting("form", "structure", "floorPlanImage")
                .containsExactlyInAnyOrder(
                        tuple(officetel, openOneRoom, url),
                        tuple(officetel, openOneRoom, url)
                );
    }

    @Test
    @DisplayName("도면 상세 조회 시 images_json 파싱 예외가 발생하면 대표 이미지 fallback을 반환한다")
    void getExploreHouseTemplateDetail_returnsFallbackWhenJsonParseFails() {
        FloorPlan floorPlan = FloorPlan.builder()
                .id(1L)
                .floorPlanName("다용도실이 있는 원룸")
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.BETWEEN_6_10)
                .url("https://fallback-image")
                .filename("fallback.png")
                .originalFilename("fallback-origin.png")
                .fileExtension("png")
                .imagesJson("invalid-json")
                .build();

        when(floorPlanRepository.findById(1L)).thenReturn(java.util.Optional.of(floorPlan));
        when(floorPlanImageJsonCodec.read("invalid-json"))
                .thenThrow(new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION));

        ExploreHouseTemplateDetailResponse result = floorPlanService.getExploreHouseTemplateDetail(1L);

        assertThat(result.floorPlans()).hasSize(1);
        assertThat(result.floorPlans().getFirst().imageUrl()).isEqualTo("https://fallback-image");
        assertThat(result.floorPlans().getFirst().view()).isNull();
    }

    @Test
    @DisplayName("도면 전체 조회 시 images_json 파싱 예외가 발생해도 fallback 이미지로 응답한다")
    void getExploreHouseTemplates_returnsFallbackWhenJsonParseFails() {
        FloorPlan floorPlan = FloorPlan.builder()
                .id(1L)
                .floorPlanName("일자형 원룸")
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.BETWEEN_6_10)
                .url("https://fallback-image")
                .filename("fallback.png")
                .originalFilename("fallback-origin.png")
                .fileExtension("png")
                .imagesJson("invalid-json")
                .build();

        when(floorPlanRepository.findAll()).thenReturn(List.of(floorPlan));
        when(floorPlanImageJsonCodec.read("invalid-json"))
                .thenThrow(new GeneralException(ErrorCode.OBJECTMAPPER_EXCEPTION));

        ExploreHouseTemplateListResponse result = floorPlanService.getExploreHouseTemplates(
                null,
                null,
                null,
                null,
                null
        );

        assertThat(result.floorPlans()).hasSize(1);
        assertThat(result.floorPlans().getFirst().imageUrl()).isEqualTo("https://fallback-image");
    }

    @Test
    @DisplayName("다중 매핑 JSON이 있으면 공용 도면 조회는 대표 단일 컬럼이 아니라 JSON 값을 기준으로 필터링한다")
    void getHousingPlan_usesMultiMappingsWhenJsonExists() {
        FloorPlan matchingFloorPlan = FloorPlan.builder()
                .id(1L)
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .equilibrium(Equilibrium.BETWEEN_6_10)
                .formsJson("[\"APARTMENT\",\"OFFICETEL\"]")
                .structuresJson("[\"DUPLEX\"]")
                .url("https://image-1")
                .filename("file-1.png")
                .originalFilename("origin-1.png")
                .fileExtension("png")
                .build();
        FloorPlan nonMatchingFloorPlan = FloorPlan.builder()
                .id(2L)
                .form(Form.APARTMENT)
                .structure(Structure.OPEN_ONE_ROOM)
                .url("https://image-2")
                .filename("file-2.png")
                .originalFilename("origin-2.png")
                .fileExtension("png")
                .build();

        when(floorPlanRepository.findAll()).thenReturn(List.of(matchingFloorPlan, nonMatchingFloorPlan));
        when(floorPlanFormJsonCodec.read("[\"APARTMENT\",\"OFFICETEL\"]"))
                .thenReturn(List.of(Form.APARTMENT, Form.OFFICETEL));
        when(floorPlanStructureJsonCodec.read("[\"DUPLEX\"]"))
                .thenReturn(List.of(Structure.DUPLEX));
        when(floorPlanFormJsonCodec.read(null)).thenReturn(List.of());
        when(floorPlanStructureJsonCodec.read(null)).thenReturn(List.of());

        FloorPlanListResponse result = floorPlanService.getHousingPlan(Form.APARTMENT, Structure.DUPLEX);

        assertThat(result.floorPlanList()).hasSize(1);
        assertThat(result.floorPlanList().getFirst().id()).isEqualTo(1L);
    }
}
