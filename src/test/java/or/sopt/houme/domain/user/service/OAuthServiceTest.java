package or.sopt.houme.domain.user.service;

import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import or.sopt.houme.domain.user.infrastructure.client.KaKaoOAuthClient;
import or.sopt.houme.domain.user.infrastructure.client.KaKaoUserInfoClient;
import or.sopt.houme.domain.user.presentation.controller.dto.CustomUserDetails;
import or.sopt.houme.domain.user.presentation.controller.dto.KakaoLoginResponse;
import or.sopt.houme.domain.user.presentation.controller.dto.KaKaoOAuthTokenDTO;
import or.sopt.houme.domain.user.presentation.controller.dto.KaKaoUserInfoResponse;
import or.sopt.houme.domain.credit.repository.CreditRepository;
import or.sopt.houme.domain.user.model.entity.Gender;
import or.sopt.houme.domain.user.model.entity.Role;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.SignupSessionRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.domain.user.model.entity.record.SignupSession;
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
import org.springframework.dao.DataIntegrityViolationException;
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
    private CreditRepository creditRepository;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private JWTConfig jwtConfig;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private BlacklistTokenRepository blacklistTokenRepository;
    @Mock
    private SignupSessionRepository signupSessionRepository;
    @Mock
    private KaKaoConfig kaKaoConfig;
    @Mock
    private CookieConfig cookieConfig;
    @Mock
    private NicknameService nicknameService;
    @Mock
    private UserNicknameTagTransactionService userNicknameTagTransactionService;

    @Mock
    private HttpServletResponse response;

    @Test
    @DisplayName("signUpWithTokenV2는 닉네임 태그를 분리 저장하고 닉네임만 반환한다")
    void signUpWithTokenV2_success() {
        SignupSession signupSession = SignupSession.of(1L, "test@houme.kr", "카카오닉네임");
        User savedUser = User.builder()
                .id(1L)
                .name("카카오닉네임")
                .nickname("느긋한펭귄")
                .nicknameTag("#4821")
                .role(Role.ROLE_USER)
                .build();

        when(signupSessionRepository.consume("signup-token")).thenReturn(Optional.of(signupSession));
        when(userRepository.existsByEmail("test@houme.kr")).thenReturn(false);
        when(nicknameService.generateNicknameTag("느긋한펭귄")).thenReturn("#4821");
        when(userNicknameTagTransactionService.createSocialUserWithNicknameTag(
                signupSession,
                "카카오닉네임",
                "느긋한펭귄",
                "#4821",
                Gender.MALE,
                java.time.LocalDate.of(2000, 1, 1)
        )).thenReturn(savedUser);
        when(jwtConfig.getAccessTokenValidityInSeconds()).thenReturn(3600L);
        when(jwtConfig.getRefreshTokenValidityInSeconds()).thenReturn(86400L);
        when(jwtUtil.createJwt(eq("access"), eq(1L), eq("ROLE_USER"), anyLong())).thenReturn("accessToken");
        when(jwtUtil.createJwt(eq("refresh"), eq(1L), eq("ROLE_USER"), anyLong())).thenReturn("refreshToken");
        when(cookieConfig.getDomain()).thenReturn("domain");
        when(cookieConfig.getSameSite()).thenReturn("true");

        String result = oAuthService.signUpWithTokenV2(
                "signup-token",
                "느긋한펭귄",
                Gender.MALE,
                java.time.LocalDate.of(2000, 1, 1),
                response
        );

        assertEquals("느긋한펭귄", result);
        verify(userNicknameTagTransactionService).createSocialUserWithNicknameTag(
                signupSession,
                "카카오닉네임",
                "느긋한펭귄",
                "#4821",
                Gender.MALE,
                java.time.LocalDate.of(2000, 1, 1)
        );
    }

    @Test
    @DisplayName("signUpWithTokenV2는 닉네임 태그 유니크 충돌이 나면 재시도한다")
    void signUpWithTokenV2_retryOnNicknameTagConstraintViolation() {
        SignupSession signupSession = SignupSession.of(1L, "test@houme.kr", "카카오닉네임");
        User savedUser = User.builder()
                .id(1L)
                .name("카카오닉네임")
                .nickname("느긋한펭귄")
                .nicknameTag("#5678")
                .role(Role.ROLE_USER)
                .build();

        when(signupSessionRepository.consume("signup-token")).thenReturn(Optional.of(signupSession));
        when(userRepository.existsByEmail("test@houme.kr")).thenReturn(false);
        when(nicknameService.generateNicknameTag("느긋한펭귄")).thenReturn("#1234", "#5678");
        when(userNicknameTagTransactionService.createSocialUserWithNicknameTag(
                signupSession,
                "카카오닉네임",
                "느긋한펭귄",
                "#1234",
                Gender.MALE,
                java.time.LocalDate.of(2000, 1, 1)
        )).thenThrow(new DataIntegrityViolationException("uk_user_nickname_nickname_tag"));
        when(userNicknameTagTransactionService.createSocialUserWithNicknameTag(
                signupSession,
                "카카오닉네임",
                "느긋한펭귄",
                "#5678",
                Gender.MALE,
                java.time.LocalDate.of(2000, 1, 1)
        )).thenReturn(savedUser);
        when(jwtConfig.getAccessTokenValidityInSeconds()).thenReturn(3600L);
        when(jwtConfig.getRefreshTokenValidityInSeconds()).thenReturn(86400L);
        when(jwtUtil.createJwt(eq("access"), eq(1L), eq("ROLE_USER"), anyLong())).thenReturn("accessToken");
        when(jwtUtil.createJwt(eq("refresh"), eq(1L), eq("ROLE_USER"), anyLong())).thenReturn("refreshToken");
        when(cookieConfig.getDomain()).thenReturn("domain");
        when(cookieConfig.getSameSite()).thenReturn("true");

        String result = oAuthService.signUpWithTokenV2(
                "signup-token",
                "느긋한펭귄",
                Gender.MALE,
                java.time.LocalDate.of(2000, 1, 1),
                response
        );

        assertEquals("느긋한펭귄", result);
        verify(nicknameService, times(2)).generateNicknameTag("느긋한펭귄");
    }


    @Test
    @DisplayName("카카오 인증 요청 URL을 생성한다")
    void requestRedirectTest() {
        when(kaKaoConfig.getClientId()).thenReturn("client-id");
        when(kaKaoConfig.getScope()).thenReturn("profile_nickname,account_email");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Origin")).thenReturn("http://localhost:5173");

        String result = oAuthService.requestRedirect(request);

        String expectedRedirect = java.net.URLEncoder.encode("http://localhost:5173/oauth/kakao/callback", java.nio.charset.StandardCharsets.UTF_8);
        String expectedScope = java.net.URLEncoder.encode("profile_nickname,account_email", java.nio.charset.StandardCharsets.UTF_8);

        assertEquals(
                "https://kauth.kakao.com/oauth/authorize?client_id=client-id&redirect_uri=" + expectedRedirect + "&response_type=code&scope=" + expectedScope,
                result
        );
    }


    @Test
    @DisplayName("kakaoLogin() 신규회원이면 회원을 생성하지 않고 signupToken을 발급한다")
    void kakaoLogin_newUser_issueSignupToken() {
        // Given
        String code = "authCode";
        KaKaoOAuthTokenDTO tokenDTO = new KaKaoOAuthTokenDTO();
        tokenDTO.setAccess_token("kakaoAccessToken");

        KaKaoUserInfoResponse.KakaoAccount kakaoAccount = new KaKaoUserInfoResponse.KakaoAccount();
        kakaoAccount.setEmail("test@houme.kr");

        KaKaoUserInfoResponse.KakaoAccount.Profile profile = new KaKaoUserInfoResponse.KakaoAccount.Profile();
        profile.setNickname("테스트닉네임");
        kakaoAccount.setProfile(profile);

        KaKaoUserInfoResponse userInfo = new KaKaoUserInfoResponse();
        userInfo.setId(1234L);
        userInfo.setKakao_account(kakaoAccount);

        when(kaKaoOAuthClient.getToken(any(), any(), any(), any())).thenReturn(tokenDTO);
        when(kaKaoUserInfoClient.getUserInfo("Bearer kakaoAccessToken")).thenReturn(userInfo);
        when(userRepository.existsByEmail("test@houme.kr")).thenReturn(false);
        ReflectionTestUtils.setField(oAuthService, "signupTokenTtlSeconds", 600L);

        // When
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Origin")).thenReturn("http://localhost:5173");

        KakaoLoginResponse result = oAuthService.kakaoLogin(code, request, response);

        // Then
        assertTrue(result.isNewUser());
        assertNotNull(result.signupToken());
        assertEquals("test@houme.kr", result.prefill().email());
        assertEquals("테스트닉네임", result.prefill().nickname());

        verify(signupSessionRepository).save(anyString(), any(or.sopt.houme.domain.user.model.entity.record.SignupSession.class), eq(600L));
        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenRepository, never()).saveRefreshToken(anyLong(), anyString(), anyLong());
        verify(response, never()).setHeader(eq("access-token"), anyString());
    }

    @Test
    @DisplayName("kakaoLogin() 기존회원이면 access/refresh 토큰을 발급한다")
    void kakaoLogin_existingUser_issueJwtTokens() {
        // Given
        String code = "authCode";
        KaKaoOAuthTokenDTO tokenDTO = new KaKaoOAuthTokenDTO();
        tokenDTO.setAccess_token("kakaoAccessToken");

        KaKaoUserInfoResponse.KakaoAccount kakaoAccount = new KaKaoUserInfoResponse.KakaoAccount();
        kakaoAccount.setEmail("test@houme.kr");

        KaKaoUserInfoResponse userInfo = new KaKaoUserInfoResponse();
        userInfo.setId(1234L);
        userInfo.setKakao_account(kakaoAccount);

        when(kaKaoOAuthClient.getToken(any(), any(), any(), any())).thenReturn(tokenDTO);
        when(kaKaoUserInfoClient.getUserInfo("Bearer kakaoAccessToken")).thenReturn(userInfo);
        when(userRepository.existsByEmail("test@houme.kr")).thenReturn(true);
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
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Origin")).thenReturn("http://localhost:5173");

        KakaoLoginResponse result = oAuthService.kakaoLogin(code, request, response);

        // Then
        assertFalse(result.isNewUser());
        verify(refreshTokenRepository).saveRefreshToken(eq(1L), eq("refreshToken"), eq(86400L));
        verify(response).setHeader("access-token", "accessToken");
    }


    @Test
    @DisplayName("kakaoLogin() 중, 인가 코드가 null이면 정해진 예외가 발생한다")
    void whenNullCode_thenThrowException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        assertThrows(UserException.class, () -> oAuthService.kakaoLogin(null, request, response));
    }

    @Test
    @DisplayName("kakaoLogin() 중, 인가 코드가 빈 문자열이면 정해진 예외가 발생한다")
    void whenEmptyCode_thenThrowException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        assertThrows(UserException.class, () -> oAuthService.kakaoLogin("", request, response));
    }

    @Test
    @DisplayName("kakaoLogin() 중, FeignException이 발생하면 정해진 예외가 발생한다")
    void whenFeignOnToken_thenThrowException() {
        String accessCode = "fake_code";
        when(kaKaoOAuthClient.getToken(any(), any(), any(), any()))
                .thenThrow(FeignException.class);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Origin")).thenReturn("http://localhost:5173");
        assertThrows(UserException.class, () -> oAuthService.kakaoLogin(accessCode, request, response));
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

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Origin")).thenReturn("http://localhost:5173");
        assertThrows(UserException.class, () -> oAuthService.kakaoLogin(accessCode, request, response));
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
