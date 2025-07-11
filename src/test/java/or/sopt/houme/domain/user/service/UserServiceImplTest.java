package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.generateImage.entity.GenerateImage;
import or.sopt.houme.domain.generateImage.repository.GenerateImageRepository;
import or.sopt.houme.domain.house.entity.House;
import or.sopt.houme.domain.house.entity.enums.Equilibrium;
import or.sopt.houme.domain.house.entity.enums.Form;
import or.sopt.houme.domain.house.repository.HouseRepository;
import or.sopt.houme.domain.taste.entity.Tag;
import or.sopt.houme.domain.taste.repository.TagRepository;
import or.sopt.houme.domain.user.controller.dto.*;
import or.sopt.houme.domain.user.entity.Gender;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.GenerateImageException;
import or.sopt.houme.global.api.handler.HouseException;
import or.sopt.houme.global.api.handler.TagException;
import or.sopt.houme.global.api.handler.UserException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;

class UserServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final HouseRepository houseRepository = mock(HouseRepository.class);
    private final TagRepository tagRepository = mock(TagRepository.class);
    private final GenerateImageRepository generateImageRepository = mock(GenerateImageRepository.class);

    private final UserServiceImpl userService = new UserServiceImpl(userRepository, houseRepository, tagRepository, generateImageRepository);

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
                .tagName("모던")
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
    @DisplayName("유저의 이미지 생성 이력 조회 성공")
    void getUserImageHistoryList_Success() {
        // given
        User mockUser = User.builder().id(1L).build();
        GenerateImage mockImage = GenerateImage.builder()
                .id(1L)
                .url("test.png")
                .build();
        List<UserImageHistoryDTO> mockHistories = List.of(
                new UserImageHistoryDTO("url1.png", "모던", "5평 이하", "원룸"),
                new UserImageHistoryDTO("url2.png", "빈티지", "6~10평", "단독주택")
        );

        // 유저조회 -> mockUser반환
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));

        // 이미지 생성한적 있는지 조회 -> mockImage 반환
        given(userRepository.findImageHistoryById(1L)).willReturn(Optional.of(mockImage));

        // 이미지 생성 이력 리스트 조회 -> mockHistories 반환
        given(userRepository.getUserImageHistory(1L)).willReturn(mockHistories);

        // when
        // mockUser에 대한 이미지 생성이력 조회 로직 실행
        UserImageHistoryListResponse response = userService.getUserImageHistoryList(mockUser);

        // then
        // 로직이 정상 수행되는지, 예상한 배열의 길이인지, 예상한 값과 같은지 검증
        assertThat(response).isNotNull();
        assertThat(response.histories()).hasSize(2);
        assertThat(response.histories().get(0).generatedImageUrl()).isEqualTo("url1.png");
    }

    @Test
    @DisplayName("마이페이지 이미지 히스토리 결과 페이지 조회 성공")
    void getImageHistoryResultPage_success() {
        // given
        Long userId = 1L;
        Long imageId = 10L;

        User user = User.builder()
                .id(userId)
                .name("테스트유저")
                .build();

        House house = House.builder()
                .form(Form.OFFICETEL)
                .equilibrium(Equilibrium.UNDER_5)
                .build();

        Tag tag = Tag.builder()
                .tagName("모던")
                .build();

        GenerateImage generateImage = GenerateImage.builder()
                .url("https://example.com/image.png")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(houseRepository.findHouseByUserIdAndImageId(userId, imageId)).willReturn(Optional.of(house));
        given(tagRepository.findTagByUserIdAndImageId(userId, imageId)).willReturn(Optional.of(tag));
        given(generateImageRepository.findGenerateImageByUserIdAndImageId(userId, imageId)).willReturn(Optional.of(generateImage));

        // when
        ImageHistoryResultPageResponse response = userService.getImageHistoryResultPage(user, imageId);

        // then
        assertThat(response.equilibrium()).isEqualTo("UNDER_5");
        assertThat(response.houseForm()).isEqualTo("OFFICETEL");
        assertThat(response.tasteTag()).isEqualTo("모던");
        assertThat(response.name()).isEqualTo("테스트유저");
        assertThat(response.generatedImageUrl()).isEqualTo("https://example.com/image.png");
    }

    @Test
    @DisplayName("house를 찾을 수 없는 경우 예외 발생")
    void getImageHistoryResultPage_notFoundHouse() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(houseRepository.findHouseByUserIdAndImageId(user.getId(), generateImage.getId()))
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
        given(houseRepository.findHouseByUserIdAndImageId(user.getId(), generateImage.getId()))
                .willReturn(Optional.of(house));
        given(tagRepository.findTagByUserIdAndImageId(user.getId(), generateImage.getId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getImageHistoryResultPage(user, generateImage.getId()))
                .isInstanceOf(TagException.class)
                .hasMessageContaining("태그 객체를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("generateImage를 찾을 수 없는 경우 예외 발생")
    void getImageHistoryResultPage_notFoundGenerateImage() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(houseRepository.findHouseByUserIdAndImageId(user.getId(), generateImage.getId()))
                .willReturn(Optional.of(house));
        given(tagRepository.findTagByUserIdAndImageId(user.getId(), generateImage.getId()))
                .willReturn(Optional.of(tag));
        given(generateImageRepository.findGenerateImageByUserIdAndImageId(user.getId(), generateImage.getId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getImageHistoryResultPage(user, generateImage.getId()))
                .isInstanceOf(GenerateImageException.class)
                .hasMessageContaining("생성된 이미지 객체를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("성공적으로_유저정보를_업데이트한다")
    void updateUser_success() {
        // given
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
                Gender.MALE,
                LocalDate.of(2000, 5, 15)
        );

        // 유저 모킹
        given(userRepository.findById(1L)).willReturn(Optional.of(dbUser));

        // when
        userService.updateUser(inputUser, request);

        // then
        assertEquals("New Name", dbUser.getName());
        assertEquals(Gender.MALE, dbUser.getGender());
        assertEquals(LocalDate.of(2000, 5, 15), dbUser.getBirthday());
    }
}
