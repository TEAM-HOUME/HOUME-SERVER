package or.sopt.houme.domain.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.entity.RefreshToken;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.TokenException;
import or.sopt.houme.global.api.UserException;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.global.util.CookieUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JWTService {


    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;


    // 토큰 발급기를 위한 메서드입니다
    public void createToken(HttpServletResponse response) {

        String access = jwtUtil.createJwt("access", 1L, "ROLE_USER", jwtConfig.getAccessTokenValidityInSeconds());
        response.setHeader("access-token", access);
    }

    /***/
    public void RefreshRotate(HttpServletRequest request, HttpServletResponse response){

        String refresh = null;

        Cookie[] cookies = request.getCookies();
        if (cookies == null) throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);

        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh-token")) {

                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {

            throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            throw new TokenException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }


        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {
            throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        Long userId = jwtUtil.getId(refresh);

        Boolean isExist = refreshTokenRepository.existsById(userId);

        if (!isExist) {

            //response body
            throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));


        String access = jwtUtil.createJwt("access", findUser.getId(), findUser.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String newRefresh = jwtUtil.createJwt("refresh", findUser.getId(), findUser.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());


        refreshTokenRepository.deleteById(userId);
        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId(findUser.getId())
                .refreshToken(newRefresh)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        response.setHeader("access-token", access);

        Cookie refreshCookie = CookieUtil.createSecureCookie("refresh-token",
                newRefresh,
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                false);

        response.addCookie(refreshCookie);

    }
}
