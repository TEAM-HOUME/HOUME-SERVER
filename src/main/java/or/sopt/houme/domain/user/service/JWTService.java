package or.sopt.houme.domain.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRedisTemplateUtil;


    // 토큰 발급기를 위한 메서드입니다
    public void createToken(HttpServletResponse response) {

        String access = jwtUtil.createJwt("access", 1L, "ROLE_USER", jwtConfig.getAccessTokenValidityInSeconds());
        response.setHeader("access-token", access);
    }


    /**
     * 액세스 토큰이 만료가 되면 발생하는 예외를 클라이언트가 받게되면
     * 서버에 RTT 로직을 호출합니다.
     *
     * 그러면 서버에서는 해당 로직을 수행합니다
     *
     * 만약 여기서 걸리는 경우가 존재한다면, 새롭게 로그인을 하여 리프레시 토큰을 발급받아야 합니다.
     * */
    public void RefreshRotate(HttpServletRequest request, HttpServletResponse response){

        String refresh = null;

        // 0. 쿠키를 탐색
        Cookie[] cookies = request.getCookies();
        if (cookies == null) throw new TokenException(ErrorCode.COOKIE_NULL);

        // 1. 쿠키를 순차적으로 돌면서 우리가 이전에 설정해놓은 쿠키의 키값과 일치하는 쿠키가 존재하는지 탐색
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh-token")) {

                refresh = cookie.getValue();
            }
        }

        // 1-1. 쿠키 속에 우리가 만들어놓은 리프레시 토큰이 존재하지 않는다면 예외 발생
        if (refresh == null) {

            throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        // 1-2. 토큰의 유효기간이 만료되었다면 예외 발생
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            throw new TokenException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 2. 발견한 토큰의 카테고리가 refresh 가 맞는지 확인
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {
            throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        // 3. 리프레시 토큰에서 ID를 가져와서 해당 토큰이 서버에 존재하는지 확인
        Long userId = jwtUtil.getId(refresh);

        Boolean isExist = refreshTokenRedisTemplateUtil.existsById(userId);

        if (!isExist) {
            throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);
        }


        /**
         * 위의 모든 검증절차를 통과하였다면
         * 기존의 리프레시 토큰은 서버에서 삭제하고
         *
         * 액세스 토큰과 리프레시 토큰을 새롭게 발급합니다
         * */
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));


        String access = jwtUtil.createJwt("access", findUser.getId(), findUser.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String newRefresh = jwtUtil.createJwt("refresh", findUser.getId(), findUser.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());


        refreshTokenRedisTemplateUtil.deleteById(userId);

        refreshTokenRedisTemplateUtil.saveRefreshToken(userId,newRefresh,jwtConfig.getRefreshTokenValidityInSeconds());

        response.setHeader("access-token", access);

        Cookie refreshCookie = CookieUtil.createSecureCookie("refresh-token",
                newRefresh,
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                false);

        response.addCookie(refreshCookie);

    }
}
