package or.sopt.houme.domain.user.valid;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TokenException;
import or.sopt.houme.global.jwt.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenValidatorTest {


    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenValidator refreshTokenValidator;



    @Test
    @DisplayName("validateRefreshToken()는 유효한 토큰이 들어오면 회원 id를 반환한다")
    void validateRefreshToken_success() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "valid-refresh-token";
        Cookie cookie = new Cookie("refresh-token", token);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        when(jwtUtil.isExpired(token)).thenReturn(false); // 내부에서 void일 경우 doNothing()
        when(jwtUtil.getCategory(token)).thenReturn("refresh");
        when(jwtUtil.getId(token)).thenReturn(123L);
        when(refreshTokenRepository.existsById(123L)).thenReturn(true);

        // when
        Long result = refreshTokenValidator.validateRefreshToken(request);

        // then
        assertEquals(123L, result);
    }


    @Test
    @DisplayName("validateRefreshToken()는 쿠키를 찾지 못하면 정해진 예외가 발생한다")
    void validateRefreshToken_cookieNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        TokenException e = assertThrows(TokenException.class, () ->
                refreshTokenValidator.validateRefreshToken(request)
        );

        assertEquals(ErrorCode.COOKIE_NULL, e.getErrorCode());
    }


    @Test
    @DisplayName("validateRefreshToken()는 쿠키안에 리프레시 토큰이 없다면 정해진 예외를 반환한다")
    void validateRefreshToken_noRefreshTokenCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("other", "abc")});

        TokenException e = assertThrows(TokenException.class, () ->
                refreshTokenValidator.validateRefreshToken(request)
        );

        assertEquals(ErrorCode.REFRESH_TOKEN_NULL, e.getErrorCode());
    }


    @Test
    @DisplayName("validateRefreshToken()는 서명이 위조된 토큰이 있다면 정해진 예외를 반환한다")
    void validateRefreshToken_signatureException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "invalid-signature-token";
        Cookie cookie = new Cookie("refresh-token", token);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        doThrow(new io.jsonwebtoken.security.SignatureException("Invalid"))
                .when(jwtUtil).isExpired(token);

        TokenException e = assertThrows(TokenException.class, () ->
                refreshTokenValidator.validateRefreshToken(request)
        );

        assertEquals(ErrorCode.INVALID_SIGNATURE, e.getErrorCode());
    }


    @Test
    @DisplayName("validateRefreshToken()는 토큰이 만료되면 정해진 예외를 반환한다")
    void validateRefreshToken_expired() {
        // given
        JWTUtil jwtUtil = mock(JWTUtil.class);
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshTokenValidator validator = new RefreshTokenValidator(jwtUtil, repository);

        String token = "expired-refresh-token";
        Cookie cookie = new Cookie("refresh-token", token);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        // 만료된 토큰이므로 isExpired 에서 예외를 던지도록 설정
        doThrow(new ExpiredJwtException(null, null, "Token expired"))
                .when(jwtUtil).isExpired(token);

        // when & then
        TokenException e = assertThrows(TokenException.class, () ->
                validator.validateRefreshToken(request)
        );

        assertEquals(ErrorCode.REFRESH_TOKEN_EXPIRED, e.getErrorCode());
    }

}
