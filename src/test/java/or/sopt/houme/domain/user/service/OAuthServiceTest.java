package or.sopt.houme.domain.user.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletResponse;
import or.sopt.houme.domain.user.client.KaKaoOAuthClient;
import or.sopt.houme.domain.user.client.KaKaoUserInfoClient;
import or.sopt.houme.domain.user.controller.dto.KaKaoOAuthTokenDTO;
import or.sopt.houme.domain.user.controller.dto.KaKaoUserInfoResponse;
import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.BlacklistTokenRepository;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.handler.UserException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private HttpServletResponse response;



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
}
