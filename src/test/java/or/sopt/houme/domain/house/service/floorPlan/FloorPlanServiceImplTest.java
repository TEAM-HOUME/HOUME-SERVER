package or.sopt.houme.domain.house.service.floorPlan;

import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateDetailResponse;
import or.sopt.houme.domain.house.presentation.floorPlan.dto.response.ExploreHouseTemplateListResponse;
import or.sopt.houme.domain.house.model.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.house.repository.floorPlan.FloorPlanRepository;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.model.entity.enums.Structure;
import or.sopt.houme.domain.user.util.floorplan.FloorPlanImageJsonCodec;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

        when(floorPlanRepository.findAllByStructure(openOneRoom)).thenReturn(List.of(floorPlan1, floorPlan3));

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
}
