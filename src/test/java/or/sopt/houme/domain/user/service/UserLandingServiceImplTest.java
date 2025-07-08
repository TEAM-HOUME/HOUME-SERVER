package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.domain.user.service.UserLandingServiceImpl;
import or.sopt.houme.domain.user.valid.RefreshTokenValidator;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLandingServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenValidator refreshTokenValidator;

    @InjectMocks
    private UserLandingServiceImpl userLandingService;

    @Test
    @DisplayName("getHasGeneratedImage()는 회원의 이미지 생성이력을 Boolean 타입으로 반환 할 수 있다")
    void getHasGeneratedImage_success() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Long mockUserId = 1L;

        User mockUser = User.builder()
                .id(mockUserId)
                .hasGeneratedImage(true)
                .build();

        when(refreshTokenValidator.validateRefreshToken(request)).thenReturn(mockUserId);
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        // when
        Boolean result = userLandingService.getHasGeneratedImage(request);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("getHasGeneratedImage() 는 회원을 찾을 수 없으면 정해진 예외를 반환한다")
    void getHasGeneratedImage_userNotFound() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Long mockUserId = 999L;

        when(refreshTokenValidator.validateRefreshToken(request)).thenReturn(mockUserId);
        when(userRepository.findById(mockUserId)).thenReturn(Optional.empty());

        // when & then
        UserException e = assertThrows(UserException.class, () ->
                userLandingService.getHasGeneratedImage(request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
    }
}
