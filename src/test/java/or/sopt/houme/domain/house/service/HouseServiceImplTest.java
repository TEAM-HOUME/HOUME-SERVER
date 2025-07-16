package or.sopt.houme.domain.house.service;

import or.sopt.houme.domain.house.dto.HouseOptionDTO;
import or.sopt.houme.domain.house.dto.LatestHouseConditionDTO;
import or.sopt.houme.domain.house.dto.response.HouseOptionsResponse;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Activity;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.entity.enums.Structure;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.user.entity.*;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("[House Service Test]")
class HouseServiceImplTest {

    @InjectMocks
    private HouseServiceImpl houseService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HouseRepository houseRepository;

    private User savedUser;
    private House savedHouse;

    @BeforeEach
    void setUp() {
        savedUser = User.builder()
                .id(1L)
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

        savedHouse = House.builder()
                .id(1L)
                        .form(Form.OFFICETEL)
                        .structure(Structure.OPEN_ONE_ROOM)
                        .equilibrium(Equilibrium.UNDER_5)
                        .isValid(true)
                        .user(savedUser)
                        .build();
    }

    @Test
    @DisplayName("Enum → DTO 변환: housingTypes, roomTypes, areaTypes가 모두 Enum 기준으로 정확하게 반환된다.")
    void getHouseOptionsResponse_ShouldReturnValidDTOs() {

        // When
        HouseOptionsResponse response = houseService.getHouseOptionsResponse();

        // Then
        var formCodes = Arrays.stream(Form.values())
                .map(Enum::name)
                .toList();
        assertThat(response.housingTypes())
                .hasSize(formCodes.size())
                .extracting(HouseOptionDTO::code)
                .containsExactlyElementsOf(formCodes);

        var structureCodes = Arrays.stream(Structure.values())
                .map(Enum::name)
                .toList();
        assertThat(response.roomTypes())
                .hasSize(structureCodes.size())
                .extracting(HouseOptionDTO::code)
                .containsExactlyElementsOf(structureCodes);

        var equilibriumCodes = Arrays.stream(Equilibrium.values())
                .map(Enum::name)
                .toList();
        assertThat(response.areaTypes())
                .hasSize(equilibriumCodes.size())
                .extracting(HouseOptionDTO::code)
                .containsExactlyElementsOf(equilibriumCodes);
    }

    @Test
    @DisplayName("User를 받아서 최근에 입력한 House 조건들을 받을 수 있다.")
    void getHouseOptionsResponse_ShouldReturnValidHouse() {
        // Given
        when(houseRepository.findLatestHouse(savedUser)).thenReturn(savedHouse);

        // When
        LatestHouseConditionDTO latestHouse = houseService.findLatestHouse(savedUser);

        // Then
        assertThat(latestHouse).isNotNull();
        assertThat(latestHouse)
                .extracting("form", "structure", "equilibrium")
                .contains(Form.OFFICETEL, Structure.OPEN_ONE_ROOM, Equilibrium.UNDER_5);
    }

    @Test
    @DisplayName("[Exception] 생성되어있는 house가 없는 경우 예외가 발생한다.")
    void getHousingPlanNoHouse() {
        // Given
        User user = User.builder()
                .id(2L)
                .build();

        // When // Then
        assertThatThrownBy(() -> houseService.findLatestHouse(user))
                .isInstanceOf(GeneralException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_HOUSE.getMsg());
    }

    @Test
    @DisplayName("house activity 업데이트")
    void updateHouseActivity() {
        // Given
        when(houseRepository.findById(savedHouse.getId())).thenReturn(Optional.of(savedHouse));
        when(houseRepository.save(savedHouse)).thenReturn(savedHouse);

        // When
        House house = houseService.updateHouseActivity(savedHouse.getId(), Activity.RELAXING);

        // Then
        assertThat(house).isNotNull();
        assertThat(house.getActivity()).isEqualTo(Activity.RELAXING);
    }
}