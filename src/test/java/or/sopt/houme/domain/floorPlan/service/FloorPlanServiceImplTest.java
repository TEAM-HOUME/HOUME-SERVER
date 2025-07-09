package or.sopt.houme.domain.floorPlan.service;

import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanResponse;
import or.sopt.houme.domain.floorPlan.entity.FloorPlan;
import or.sopt.houme.domain.floorPlan.repository.FloorPlanRepository;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[FloorPlan Service Test]")
class FloorPlanServiceImplTest {

    @Autowired
    FloorPlanService floorPlanService;

    @Autowired
    FloorPlanRepository floorPlanRepository;

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
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .floorPlanPrompt("prompt1")
                .url(url)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(fileExtension)
                .build();
        FloorPlan floorPlan2 = FloorPlan.builder()
                .form(Form.ETC)
                .structure(Structure.DUPLEX)
                .floorPlanPrompt("prompt2")
                .url(url)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(fileExtension)
                .build();
        FloorPlan floorPlan3 = FloorPlan.builder()
                .form(Form.OFFICETEL)
                .structure(Structure.OPEN_ONE_ROOM)
                .floorPlanPrompt("prompt3")
                .url(url)
                .filename(filename)
                .originalFilename(originalFilename)
                .fileExtension(fileExtension)
                .build();

        floorPlanRepository.saveAll(List.of(floorPlan1, floorPlan2, floorPlan3));

        // When
        List<FloorPlanResponse> housingPlan = floorPlanService.getHousingPlan(officetel, openOneRoom, under5);

        // Then
        assertThat(housingPlan).isNotNull();
        assertThat(housingPlan.size()).isEqualTo(2);
        assertThat(housingPlan.get(0))
                .extracting("id", "form", "structure", "floorPlanImage")
                .contains(1L, officetel, openOneRoom, url);
        assertThat(housingPlan).hasSize(2)
                .extracting("form", "structure", "floorPlanImage")
                .containsExactlyInAnyOrder(
                        tuple(officetel, openOneRoom, url),
                        tuple(officetel, openOneRoom, url)
                );
    }
}