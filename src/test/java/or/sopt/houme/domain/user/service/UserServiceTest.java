package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.generatedImage.entity.GenerateImage;
import or.sopt.houme.domain.user.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryDTO;
import or.sopt.houme.domain.user.controller.dto.UserImageHistoryListResponse;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserService userService = new UserService(userRepository);

    @Test
    @DisplayName("✅ 마이페이지 유저 정보 조회 성공")
    void getMyPageInfo_success() {
        // given
        User user = User.builder()
                .id(1L)
                .name("테스트유저")
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.countByMemberIdAndStatus(1L)).willReturn(10L);

        // when
        MyPageInfoResponse response = userService.getMyPageInfo(user);

        // then
        assertThat(response.name()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("❌ 유저 정보가 없을 경우 예외 발생")
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
    @DisplayName("✅ 유저의 이미지 생성 이력 조회 성공")
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
}
