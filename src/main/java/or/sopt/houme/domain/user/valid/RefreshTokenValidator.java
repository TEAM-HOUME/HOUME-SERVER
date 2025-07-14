package or.sopt.houme.domain.user.valid;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TokenException;
import or.sopt.houme.global.jwt.JWTUtil;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenValidator {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public Long validateRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) throw new TokenException(ErrorCode.COOKIE_NULL);

        String refresh = null;

        for (Cookie cookie : cookies) {
            if ("refresh-token".equals(cookie.getName())) {
                refresh = cookie.getValue();
            }
        }

        log.info("old refresh token: {}", refresh);
        if (refresh == null) throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);

        try {

            jwtUtil.isExpired(refresh);

            if (!"refresh".equals(jwtUtil.getCategory(refresh))) {
                throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);
            }

            Long userId = jwtUtil.getId(refresh);
            if (!refreshTokenRepository.existsById(userId)) {
                throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);
            }

            return userId;

        } catch (ExpiredJwtException e) {
            throw new TokenException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        } catch (SignatureException e) {
            throw new TokenException(ErrorCode.INVALID_SIGNATURE);
        } catch (Exception e) {
            throw new TokenException(ErrorCode.INVALID_TOKEN);
        }
    }
}
