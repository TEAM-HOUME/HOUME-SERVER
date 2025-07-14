package or.sopt.houme.domain.user.service;

import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import or.sopt.houme.domain.user.client.KaKaoOAuthClient;
import or.sopt.houme.domain.user.client.KaKaoUserInfoClient;
import or.sopt.houme.domain.user.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.controller.dto.KaKaoOAuthTokenDTO;
import or.sopt.houme.domain.user.controller.dto.KaKaoUserInfoResponse;
import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.handler.UserException;
import or.sopt.houme.global.config.CookieConfig;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.config.KaKaoConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

    @InjectMocks
    private OAuthService oAuthService;

    @Mock
    private KaKaoOAuthClient kaKaoOAuthClient;
    @Mock
    private KaKaoUserInfoClient kaKaoUserInfoClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private JWTConfig jwtConfig;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private BlacklistTokenRepository blacklistTokenRepository;
    @Mock
    private KaKaoConfig kaKaoConfig;
    @Mock
    private CookieConfig cookieConfig;

    @Mock
    private HttpServletResponse response;


    @Test
    @DisplayName("카카오 인증 요청 URL을 생성한다")
    void requestRedirectTest() {
        when(kaKaoConfig.getClientId()).thenReturn("client-id");
        when(kaKaoConfig.getRedirectUri()).thenReturn("http://localhost:3000/oauth/kakao/callback");

        String result = oAuthService.requestRedirect();

        assertEquals(
                "https://kauth.kakao.com/oauth/authorize?client_id=client-id&redirect_uri=http://localhost:3000/oauth/kakao/callback&response_type=code",
                result
        );
    }


    @Test
    @DisplayName("kakaoLogin() 을 이용해서 소셜로그인을 통해 회원을 생성 할 수 있다")
    void kakaoLogin_success() {
        // Given
        String code = "authCode";
        KaKaoOAuthTokenDTO tokenDTO = new KaKaoOAuthTokenDTO();
        tokenDTO.setAccess_token("kakaoAccessToken");

        KaKaoUserInfoResponse.KakaoAccount kakaoAccount = new KaKaoUserInfoResponse.KakaoAccount();
        kakaoAccount.setEmail("test@houme.kr");

        KaKaoUserInfoResponse.Properties properties = new KaKaoUserInfoResponse.Properties();
        properties.setNickname("테스트닉네임");

        KaKaoUserInfoResponse userInfo = new KaKaoUserInfoResponse();
        userInfo.setKakao_account(kakaoAccount);
        userInfo.setProperties(properties);

        when(kaKaoOAuthClient.getToken(any(), any(), any(), any())).thenReturn(tokenDTO);
        when(kaKaoUserInfoClient.getUserInfo("Bearer kakaoAccessToken")).thenReturn(userInfo);
        when(userRepository.existsByEmail("test@houme.kr")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmail("test@houme.kr")).thenReturn(Optional.of(
                User.builder().id(1L).email("test@houme.kr").role(Role.ROLE_USER).build()
        ));

        when(jwtUtil.createJwt(eq("access"), eq(1L), eq("ROLE_USER"), anyLong())).thenReturn("accessToken");
        when(jwtUtil.createJwt(eq("refresh"), eq(1L), eq("ROLE_USER"), anyLong())).thenReturn("refreshToken");

        when(jwtConfig.getAccessTokenValidityInSeconds()).thenReturn(3600L);
        when(jwtConfig.getRefreshTokenValidityInSeconds()).thenReturn(86400L);
        when(cookieConfig.getDomain()).thenReturn("domain");
        when(cookieConfig.getSameSite()).thenReturn("true");

        // When
        Boolean result = oAuthService.kakaoLogin(code, response);

        // Then
        assertTrue(result); // 신규 회원이므로 true
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).saveRefreshToken(eq(1L), eq("refreshToken"), eq(86400L));
        verify(response).setHeader("access-token", "accessToken");
    }


    @Test
    @DisplayName("kakaoLogin() 중, 인가 코드가 null이면 정해진 예외가 발생한다")
    void whenNullCode_thenThrowException() {
        assertThrows(UserException.class, () -> oAuthService.kakaoLogin(null, response));
    }

    @Test
    @DisplayName("kakaoLogin() 중, 인가 코드가 빈 문자열이면 정해진 예외가 발생한다")
    void whenEmptyCode_thenThrowException() {
        assertThrows(UserException.class, () -> oAuthService.kakaoLogin("", response));
    }

    @Test
    @DisplayName("kakaoLogin() 중, FeignException이 발생하면 정해진 예외가 발생한다")
    void whenFeignOnToken_thenThrowException() {
        String accessCode = "fake_code";
        when(kaKaoOAuthClient.getToken(any(), any(), any(), any()))
                .thenThrow(FeignException.class);

        assertThrows(UserException.class, () -> oAuthService.kakaoLogin(accessCode, response));
    }

    @Test
    @DisplayName("카카오 사용자 정보 요청 중 FeignException이 발생하면 정해진 예외가 발생한다")
    void whenFeignOnUserInfo_thenThrowException() {
        String accessCode = "valid_code";
        KaKaoOAuthTokenDTO tokenDTO = new KaKaoOAuthTokenDTO();
        ReflectionTestUtils.setField(tokenDTO, "access_token", "kakaoAccessToken");

        when(kaKaoOAuthClient.getToken(any(), any(), any(), any())).thenReturn(tokenDTO);
        when(kaKaoUserInfoClient.getUserInfo("Bearer kakaoAccessToken"))
                .thenThrow(FeignException.class);

        assertThrows(UserException.class, () -> oAuthService.kakaoLogin(accessCode, response));
    }


    @Test
    @DisplayName("logout()은 RefreshToken을 삭제하고, AccessToken을 블랙리스트에 저장해야 한다")
    void logout_success() {
        // given
        User mockUser = User.builder().id(1L).role(Role.ROLE_USER).build();
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "Bearer mock.jwt.token";

        when(request.getHeader(anyString())).thenReturn(token);
        when(jwtConfig.getHeader()).thenReturn("Authorization");
        when(jwtUtil.getJti(anyString())).thenReturn("jti-123");
        when(jwtUtil.getRemainingExpiration(anyString())).thenReturn(12345L);

        // when
        oAuthService.logout(userDetails, request,response);

        // then
        verify(refreshTokenRepository).deleteById(eq(1L));
        verify(blacklistTokenRepository).save(eq("jti-123"), eq(12345L));
    }


    @Test
    @DisplayName("logout()은 Authorization 헤더가 비정상일 경우 아무 작업도 하지 않는다")
    void logout_invalidHeader() {
        // given
        User mockUser = User.builder().id(1L).build();
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(anyString())).thenReturn(null); // null or not "Bearer "

        when(jwtConfig.getHeader()).thenReturn("Authorization");

        // when
        oAuthService.logout(userDetails, request,response);

        // then
        verify(refreshTokenRepository).deleteById(eq(1L));
        verify(blacklistTokenRepository, never()).save(anyString(), anyLong());
    }
}
