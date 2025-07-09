package or.sopt.houme.facade;

import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanListResponse;
import or.sopt.houme.domain.floorPlan.dto.response.FloorPlanResponse;
import or.sopt.houme.domain.floorPlan.service.FloorPlanService;
import or.sopt.houme.domain.house.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.service.HouseService;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("[FloorPlan Facade] Test")
class FloorPlanFacadeTest {

    @Autowired
    FloorPlanFacade floorPlanFacade;

    @MockBean
    private HouseService houseService;

    @MockBean
    private FloorPlanService floorPlanService;

    @Test
    @DisplayName("사용자 입력에 따른 도면들 제공")
    void getFloorPlan() {
        // Given
        Form officetel = Form.OFFICETEL;
        Structure openOneRoom = Structure.OPEN_ONE_ROOM;
        String floorPlanImage = "imageUrl";

        User user = User.builder()
                .name("test_user")
                .birthday(LocalDate.of(2001, 1, 10))
                .gender(Gender.MALE)
                .email("example.com")
                .password(null)
                .hasGeneratedImage(false)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();

        LatestHouseConditionDTO houseConditionDTO =
                new LatestHouseConditionDTO(Form.OFFICETEL, Structure.OPEN_ONE_ROOM, Equilibrium.UNDER_5);

        List<FloorPlanResponse> floorPlanResponses = List.of(
                new FloorPlanResponse(1L, officetel, openOneRoom, floorPlanImage),
                new FloorPlanResponse(2L, officetel, openOneRoom, floorPlanImage),
                new FloorPlanResponse(4L, officetel, openOneRoom, floorPlanImage));

        when(houseService.findLatestHouse(user)).thenReturn(houseConditionDTO);
        when(floorPlanService.getHousingPlan(houseConditionDTO.form(), houseConditionDTO.structure(), houseConditionDTO.equilibrium())).thenReturn(floorPlanResponses);

        // When
        FloorPlanListResponse floorPlan = floorPlanFacade.getFloorPlan(user);

        // Then
        assertThat(floorPlan).isNotNull();
        assertThat(floorPlan.floorPlanList().size()).isEqualTo(3);
        assertThat(floorPlan.floorPlanList().get(0))
                .extracting("form", "structure", "floorPlanImage")
                .contains(officetel, openOneRoom, floorPlanImage);
    }

    @Test
    @DisplayName("[Exception] 생성되어있는 house가 없는 경우 예외가 발생한다.")
    void getHousingPlanNoHouse() {
        // Given
        User user = User.builder()
                .name("test_user")
                .birthday(LocalDate.of(2001, 1, 10))
                .gender(Gender.MALE)
                .email("example.com")
                .password(null)
                .hasGeneratedImage(false)
                .socialType(SocialType.KAKAO)
                .status(UserStatus.ACTIVE)
                .role(Role.ROLE_USER)
                .build();

        when(houseService.findLatestHouse(user)).thenReturn(null);

        // When // Then
        assertThatThrownBy(() -> floorPlanFacade.getFloorPlan(user))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_HOUSE.getMsg());
    }

}