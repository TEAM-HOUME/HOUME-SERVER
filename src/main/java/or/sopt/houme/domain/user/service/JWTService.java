package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JWTService {

    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;

    public void createToken(HttpServletResponse response) {

        String access = jwtUtil.createJwt("access", 1L, "ROLE_USER", jwtConfig.getAccessTokenValidityInSeconds());
        response.setHeader("access-token", access);
    }
}
