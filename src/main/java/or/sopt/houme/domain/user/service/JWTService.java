package or.sopt.houme.domain.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.model.entity.User;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.domain.user.presentation.valid.RefreshTokenValidator;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TokenException;
import or.sopt.houme.global.api.handler.UserException;
import or.sopt.houme.global.config.CookieConfig;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.global.util.CookieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JWTService {


    private static final Logger log = LoggerFactory.getLogger(JWTService.class);
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRedisTemplateUtil;
    private final RefreshTokenValidator refreshTokenValidator;
    private final CookieConfig cookieConfig;


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
    public void refreshRotate(HttpServletRequest request, HttpServletResponse response){


        Long userIdFromRefreshToken = refreshTokenValidator.validateRefreshToken(request);

        /**
         * 위의 모든 검증절차를 통과하였다면
         * 기존의 리프레시 토큰은 서버에서 삭제하고
         *
         * 액세스 토큰과 리프레시 토큰을 새롭게 발급합니다
         * */
        User findUser = userRepository.findById(userIdFromRefreshToken)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));


        String access = jwtUtil.createJwt("access", findUser.getId(), findUser.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());
        String newRefresh = jwtUtil.createJwt("refresh", findUser.getId(), findUser.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());


        refreshTokenRedisTemplateUtil.deleteById(userIdFromRefreshToken);

        refreshTokenRedisTemplateUtil.saveRefreshToken(userIdFromRefreshToken,newRefresh,jwtConfig.getRefreshTokenValidityInSeconds());

        response.setHeader("access-token", access);

        log.info("new refresh: {}", newRefresh);
        CookieUtil.addSameSiteCookie(
                response,
                "refresh-token",
                newRefresh,
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                cookieConfig.getDomain(),
                cookieConfig.isSecure(),
                cookieConfig.getSameSite()
        );


    }
}
