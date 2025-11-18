package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.domain.user.valid.RefreshTokenValidator;
import or.sopt.houme.global.api.ErrorCode;
import or.sopt.houme.global.api.handler.TokenException;
import or.sopt.houme.global.api.handler.UserException;
import or.sopt.houme.global.jwt.JWTUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserLandingServiceImpl implements UserLandingService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public Boolean getHasGeneratedImage(HttpServletRequest request){


        /**
         * 리프레시 토큰을 검증해서
         * 쿠키가 존재하지 않거나 & 리프레시 토큰이 존재하지 않으면
         *
         * Boolean.TRUE 를 반환한다
         * */

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Boolean.TRUE;
        }

        String refresh = null;
        for (Cookie cookie : cookies) {
            if ("refresh-token".equals(cookie.getName())) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            return Boolean.TRUE;
        }



        Long userId = jwtUtil.getId(refresh);
        if (!refreshTokenRepository.existsById(userId)) {
            throw new TokenException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        User findUser = userRepository.findById(userId)
                .orElseThrow(()-> new UserException(ErrorCode.USER_NOT_FOUND));

        if (!findUser.getHasGeneratedImage()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
