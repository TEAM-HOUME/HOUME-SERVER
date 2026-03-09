package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.domain.user.presentation.valid.RefreshTokenValidator;
import or.sopt.houme.global.config.CookieConfig;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;

    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private JWTConfig jwtConfig;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenValidator refreshTokenValidator;
    @Mock
    private CookieConfig cookieConfig;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("createToken()은 access-token을 응답 헤더에 설정한다")
    void createToken_setsAccessTokenInHeader() {
        // given
        User user = User.builder().id(1L).role(Role.ROLE_USER).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtConfig.getAccessTokenValidityInSeconds()).thenReturn(3600L);
        when(jwtUtil.createJwt(eq("access"), anyLong(), anyString(), anyLong()))
                .thenReturn("accessToken");

        // when
        jwtService.createToken(response, null);

        // then
        verify(response).setHeader("access-token", "accessToken");
    }


    @Test
    @DisplayName("refreshRotate()는 리프레시 토큰을 검증하고 새 토큰을 발급 및 저장한다")
    void refreshRotate_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).role(Role.ROLE_USER).build();


        when(jwtConfig.getAccessTokenValidityInSeconds()).thenReturn(3600L);
        when(jwtConfig.getRefreshTokenValidityInSeconds()).thenReturn(86400L);
        when(refreshTokenValidator.validateRefreshToken(request)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(jwtUtil.createJwt("access", userId, "ROLE_USER", 3600L)).thenReturn("newAccessToken");
        when(jwtUtil.createJwt("refresh", userId, "ROLE_USER", 86400L)).thenReturn("newRefreshToken");

        when(cookieConfig.getDomain()).thenReturn("domain");
        when(cookieConfig.getSameSite()).thenReturn("true");

        // when
        jwtService.refreshRotate(request, response);

        // then
        verify(refreshTokenRepository).deleteById(userId);
        verify(refreshTokenRepository).saveRefreshToken(userId, "newRefreshToken", 86400L);
        verify(response).setHeader("access-token", "newAccessToken");


        verify(response).addHeader(
                eq("Set-Cookie"),
                argThat(value ->
                        value.startsWith("refresh-token=newRefreshToken") &&
                                value.contains("Max-Age=86400") &&
                                value.contains("SameSite=true") &&      // sameSite 모킹 값
                                value.contains("Domain=domain")
                )
        );

    }


    @Test
    @DisplayName("refreshRotate() 중 사용자가 존재하지 않으면 예외 발생")
    void refreshRotate_userNotFound() {
        // given
        when(refreshTokenValidator.validateRefreshToken(request)).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // then
        assertThrows(RuntimeException.class, () -> jwtService.refreshRotate(request, response));
    }
}
