package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.credit.model.entity.Credit;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.generateImage.model.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.model.entity.House;
import or.sopt.houme.domain.house.model.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.model.entity.enums.Form;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.preference.model.entity.GenerateImagePreference;
import or.sopt.houme.domain.preference.model.entity.Preference;
import or.sopt.houme.domain.preference.repository.FactorRepository;
import or.sopt.houme.domain.preference.repository.GenerateImagePreferenceRepository;
import or.sopt.houme.domain.preference.repository.PreferenceFactorRepository;
import or.sopt.houme.domain.preference.repository.PreferenceRepository;
import or.sopt.houme.domain.house.model.taste.entity.Tag;
import or.sopt.houme.domain.house.repository.taste.tag.TagRepository;
import or.sopt.houme.domain.user.presentation.controller.dto.*;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.*;

class UserServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final HouseRepository houseRepository = mock(HouseRepository.class);
    private final TagRepository tagRepository = mock(TagRepository.class);
    private final GenerateImageRepository generateImageRepository = mock(GenerateImageRepository.class);
    private final CreditRepository creditRepository = mock(CreditRepository.class);
    private final GenerateImagePreferenceRepository generateImagePreferenceRepository = mock(GenerateImagePreferenceRepository.class);
    private final FactorRepository factorRepository = mock(FactorRepository.class);
    private final PreferenceRepository preferenceRepository = mock(PreferenceRepository.class);
    private final PreferenceFactorRepository preferenceFactorRepository = mock(PreferenceFactorRepository.class);

    private final UserServiceImpl userService = new UserServiceImpl(
            userRepository,
            houseRepository,
            tagRepository,
            generateImageRepository,
            creditRepository,
            generateImagePreferenceRepository,
            factorRepository,
            preferenceRepository,
            preferenceFactorRepository
            );

    private User user;
    private House house;
    private Tag tag;
    private GenerateImage generateImage;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("테스트유저")
                .build();

        house = House.builder()
                .user(user)
                .form(Form.APARTMENT)
                .equilibrium(Equilibrium.UNDER_5)
                .build();

        tag = Tag.builder()
                .tagNameKr("모던")
                .build();

        generateImage = GenerateImage.builder()
                .id(100L)
                .url("https://cdn.com/image.png")
                .house(house)
                .build();
    }

    @Test
    @DisplayName("마이페이지 유저 정보 조회 성공")
    void getMyPageInfo_success() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.countByMemberIdAndStatus(1L)).willReturn(10L);

        // when
        MyPageInfoResponse response = userService.getMyPageInfo(user);

        // then
        assertThat(response.name()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("유저 정보가 없을 경우 예외 발생")
    void getMyPageInfo_userNotFound() {
        // given
        User user = User.builder().id(99L).build();
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMyPageInfo(user))
                .isInstanceOf(UserException.class)
                .hasMessage(ErrorCode.USER_NOT_FOUND.getMsg());
    }

    @Test
    @DisplayName("유저의 이미지 생성 이력 조회 성공 - 동일한 houseId에 여러 이미지가 있을 때 첫 번째 것만 반환")
    void getUserImageHistoryList_MultipleGenerateImages_ReturnsFirst() {
        // given
        Long userId = user.getId();

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        // 1. 유효한 house 반환
        given(houseRepository.findValidHouseByUserId(userId))
                .willReturn(List.of(house));

        // 2. 동일한 houseId에 여러 이미지가 있다고 가정
        GenerateImage firstImage = GenerateImage.builder()
                .id(100L)
                .url("https://cdn.com/image1.png")
                .house(house)
                .build();

        GenerateImage secondImage = GenerateImage.builder()
                .id(200L)
                .url("https://cdn.com/image2.png")
                .house(house)
                .build();

        // Repository는 결국 fetchFirst() 결과만 반환하므로 "첫 번째 이미지"만 리턴되도록 설정
        given(generateImageRepository.findByHouseId(house.getId()))
                .willReturn(Optional.of(firstImage));

        // 3. 대표 태그 반환
        given(tagRepository.findMostFrequentTagByHouseId(house.getId()))
                .willReturn(Optional.ofNullable(tag));

        // when
        UserImageHistoryListResponse response = userService.getUserImageHistoryList(user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.histories()).hasSize(1);

        UserImageHistoryDTO dto = response.histories().get(0);
        assertThat(dto.imageId()).isEqualTo(100L); // 첫번째 이미지가 선택됨
        assertThat(dto.generatedImageUrl()).isEqualTo("https://cdn.com/image1.png");
        assertThat(dto.tasteTag()).isEqualTo("모던");
        assertThat(dto.equilibrium()).isEqualTo("5평 이하");
        assertThat(dto.houseForm()).isEqualTo("아파트");
    }

    @Test
    @DisplayName("마이페이지 이미지 히스토리 결과 페이지 - 생성된 이미지 2개 조회 성공")
    void getImageHistoryResultPage_multipleGenerateImages_success() {
        // given
        Long userId = 1L;
        Long imageId = 10L;
        Long houseId = 20L;
        User user = User.builder()
                .id(userId)
                .name("테스트유저")
                .build();

        House house = House.builder()
                .id(houseId)
                .form(Form.OFFICETEL)
                .equilibrium(Equilibrium.UNDER_5)
                .build();

        Tag tag = Tag.builder()
                .tagNameKr("모던")
                .build();

        GenerateImage generateImage1 = GenerateImage.builder()
                .id(1L)
                .url("https://example.com/image1.png")
                .house(house)
                .build();

        GenerateImage generateImage2 = GenerateImage.builder()
                .id(2L)
                .url("https://example.com/image2.png")
                .house(house)
                .build();

        GenerateImagePreference generateImagePreference1 = GenerateImagePreference.builder()
                .preference(Preference.builder().isLike(true).build())
                .generateImage(generateImage1)
                .build();

        GenerateImagePreference generateImagePreference2 = GenerateImagePreference.builder()
                .preference(Preference.builder().isLike(true).build())
                .generateImage(generateImage2)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(houseRepository.findById(houseId)).willReturn(Optional.of(house));

        // generateImages 리스트 2개 반환
        given(generateImageRepository.findGenerateImagesByHouseId(house.getId()))
                .willReturn(List.of(generateImage1, generateImage2));

        given(generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage1.getId()))
                .willReturn(Optional.of(generateImagePreference1));
        given(generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage2.getId()))
                .willReturn(Optional.of(generateImagePreference2));

        given(tagRepository.findTagByUserIdAndImageId(userId, generateImage1.getId()))
                .willReturn(Optional.of(tag));
        given(tagRepository.findTagByUserIdAndImageId(userId, generateImage2.getId()))
                .willReturn(Optional.of(tag));

        // when
        ImageHistoriesResultPageResponse response = userService.getImageHistoryResultPage(user, houseId);

        // then
        assertThat(response.histories()).hasSize(2);

        ImageHistoriesResultPageResponse.ImageHistoryResultPageResponse history1 = response.histories().get(0);
        ImageHistoriesResultPageResponse.ImageHistoryResultPageResponse history2 = response.histories().get(1);

        assertThat(history1.generatedImageUrl()).isEqualTo("https://example.com/image1.png");
        assertThat(history2.generatedImageUrl()).isEqualTo("https://example.com/image2.png");

        // 공통 속성 검증
        response.histories().forEach(history -> {
            assertThat(history.equilibrium()).isEqualTo("5평 이하");
            assertThat(history.houseForm()).isEqualTo("OFFICETEL");
            assertThat(history.tasteTag()).isEqualTo("모던");
            assertThat(history.name()).isEqualTo("테스트유저");
            assertThat(history.isLike()).isTrue();
        });
    }

    @Test
    @DisplayName("house를 찾을 수 없는 경우 예외 발생")
    void getImageHistoryResultPage_notFoundHouse() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        // Service는 houseId로 조회하므로 findById 스텁
        given(houseRepository.findById(generateImage.getId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getImageHistoryResultPage(user, generateImage.getId()))
                .isInstanceOf(HouseException.class)
                .hasMessageContaining("집 객체를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("tag를 찾을 수 없는 경우 예외 발생")
    void getImageHistoryResultPage_notFoundTag() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(houseRepository.findById(house.getId()))
                .willReturn(Optional.of(house));
        given(generateImageRepository.findGenerateImagesByHouseId(house.getId()))
                .willReturn(List.of(generateImage));
        given(generateImagePreferenceRepository.findFirstByGenerateImageIdOrderByIdDesc(generateImage.getId()))
                .willReturn(Optional.empty());
        given(tagRepository.findTagByUserIdAndImageId(user.getId(), generateImage.getId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getImageHistoryResultPage(user, house.getId()))
                .isInstanceOf(TagException.class)
                .hasMessageContaining("태그 객체를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("generateImage를 찾을 수 없는 경우 예외 발생")
    void getImageHistoryResultPage_notFoundGenerateImage() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(houseRepository.findById(house.getId()))
                .willReturn(Optional.of(house));
        given(tagRepository.findTagByUserIdAndImageId(user.getId(), generateImage.getId()))
                .willReturn(Optional.of(tag));
        given(generateImageRepository.findGenerateImagesByHouseId(house.getId()))
                .willReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> userService.getImageHistoryResultPage(user, house.getId()))
                .isInstanceOf(GenerateImageException.class)
                .hasMessageContaining("생성된 이미지 객체를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("성공적으로_유저정보를_업데이트한다")
    void updateUser_success() {
        // given
        String male = "MALE";

        // 요청한 유저의 Id
        User inputUser = User.builder().id(1L).build();

        // DB에 있는 유저의 필드 값들
        User dbUser = User.builder()
                .id(1L)
                .name(null)
                .birthday(null)
                .gender(null)
                .build();

        // 요청
        CreateUserRequest request = CreateUserRequest.of(
                "New Name",
                male,
                LocalDate.of(2000, 5, 15).toString()
        );

        // 유저 모킹
        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));

        // when
        userService.updateUser(inputUser, request.name(), Gender.MALE, LocalDate.of(2000, 5, 15));

        // then
        assertEquals("New Name", dbUser.getName());
        assertEquals(null, dbUser.getNickname());
        assertEquals(Gender.MALE, dbUser.getGender());
        assertEquals(LocalDate.of(2000, 5, 15), dbUser.getBirthday());
    }


    @Test
    @DisplayName("성공적으로_유저정보를_업데이트하면_크레딧을 신규로 생성한다")
    void updateUser_credit_create() {
        // given
        String male = "MALE";
        // 요청한 유저의 Id
        User inputUser = User.builder().id(1L).build();

        // DB에 있는 유저의 필드 값들
        User dbUser = User.builder()
                .id(1L)
                .name(null)
                .birthday(null)
                .gender(null)
                .build();

        // 요청
        CreateUserRequest request = CreateUserRequest.of(
                "New Name",
                male,
                LocalDate.of(2000, 5, 15).toString()
        );

        // 유저 모킹
        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));

        // when
        userService.updateUser(inputUser, request.name(), Gender.MALE, LocalDate.of(2000, 5, 15));

        // then
        assertEquals("New Name", dbUser.getName());
        assertEquals(null, dbUser.getNickname());
        assertEquals(Gender.MALE, dbUser.getGender());
        assertEquals(LocalDate.of(2000, 5, 15), dbUser.getBirthday());

        verify(creditRepository, times(1))
                .saveAll(argThat(credits -> credits instanceof java.util.Collection<?> c && c.size() == 1));
    }


    @Test
    @DisplayName("크레딧 저장 중 예외가 발생하면 CreditException을 던진다")
    void updateUser_credit_create_fail() {
        // given
        String male = "MALE";
        User inputUser = User.builder().id(1L).build();

        User dbUser = User.builder()
                .id(1L)
                .name(null)
                .birthday(null)
                .gender(null)
                .build();

        CreateUserRequest request = CreateUserRequest.of(
                "New Name",
                male,
                LocalDate.of(2000, 5, 15).toString()
        );

        // 유저 조회는 정상적으로 동작
        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));

        // 크레딧 저장 시 RuntimeException 발생하도록 설정
        willThrow(new RuntimeException("DB error"))
                .given(creditRepository).saveAll(anyList());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(inputUser, request.name(), Gender.MALE, LocalDate.of(2000, 5, 15)
        ))
                .isInstanceOf(CreditException.class)
                .hasMessageContaining("크레딧 생성 과정 중 예외가 발생하였습니다.");
    }

    @Test
    @DisplayName("v2 회원가입은 닉네임 필드를 함께 업데이트한다")
    void updateUserV2_success() {
        // given
        User inputUser = User.builder().id(1L).build();

        User dbUser = User.builder()
                .id(1L)
                .name(null)
                .nickname(null)
                .birthday(null)
                .gender(null)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));

        // when
        userService.updateUserV2(inputUser, "새닉네임1234", Gender.MALE, LocalDate.of(2000, 5, 15));

        // then
        assertEquals("새닉네임1234", dbUser.getName());
        assertEquals("새닉네임1234", dbUser.getNickname());
        assertEquals(Gender.MALE, dbUser.getGender());
        assertEquals(LocalDate.of(2000, 5, 15), dbUser.getBirthday());
    }

}
