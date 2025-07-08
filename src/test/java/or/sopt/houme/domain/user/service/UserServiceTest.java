package or.sopt.houme.domain.user.service;

import or.sopt.houme.domain.user.controller.dto.MyPageInfoResponse;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
