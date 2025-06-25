package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.client.KaKaoOAuthClient;
import or.sopt.houme.domain.user.client.KaKaoUserInfoClient;
import or.sopt.houme.domain.user.controller.dto.KaKaoOAuthTokenDTO;
import or.sopt.houme.domain.user.controller.dto.KaKaoUserInfoResponse;
import or.sopt.houme.domain.user.entity.RefreshToken;
import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.domain.user.entity.SocialType;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.config.KaKaoConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final KaKaoOAuthClient kaKaoOAuthClient;
    private final KaKaoUserInfoClient kaKaoUserInfoClient;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTUtil jwtUtil;
    private final JWTConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    private final KaKaoConfig kaKaoConfig;


    /**
     * 카카오 인증 서버에 인가코드를 요청하는 메서드입니다
     *
     * 실제로는 클라이언트 단으로 주소가 리다이렉트되어 인가코드를 클라이언트에서 파싱하여 넘겨줄 것이기 떄문에 해당 메서드는 사용되지 않습니다
     * 서버에서 로직의 유효성을 검사하기 위해 사용합니다
     * */
    public String requestRedirect() {
        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                kaKaoConfig.getClientId(), kaKaoConfig.getRedirectUri()
        );
    }


    public void kakaoLogin(String accessCode, HttpServletResponse response) {

        // 인가코드를 받고 그걸 통해서 인증 액세스 토큰을 발급받습니다
        KaKaoOAuthTokenDTO authorizationCode = getKaKaoOAuthTokenDTO(accessCode);

        // 그리고 액세스 토큰을 이용하여 회원 정보를 가져옵니다
        KaKaoUserInfoResponse userInfo = kaKaoUserInfoClient.getUserInfo(
                "Bearer "+authorizationCode.getAccess_token());

        // 만약 해당 이메일을 통해 회원가입된 회원이 존재하지 않는다면, 새로운 회원을 생성합니다
        Boolean userExist = userRepository.existsByEmail(userInfo.getKakao_account().getEmail());

        if (userExist == Boolean.FALSE) {
            User newUser = User.builder()
                    .name(userInfo.getProperties().getNickname())
                    .password(bCryptPasswordEncoder.encode("kakaoPassword"))
                    .email(userInfo.getKakao_account().getEmail())
                    .role(Role.ROLE_USER)
                    .socialType(SocialType.KAKAO)
                    .build();

            userRepository.save(newUser);
        }

        // 그리고 회원 정보를 기반으로 액세스토큰을 발급하여 헤더에 넣습니다
        User byEmail = userRepository.findByEmail(userInfo.getKakao_account().getEmail());

        String access = jwtUtil.createJwt("access", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getAccessTokenValidityInSeconds());

        String refresh = jwtUtil.createJwt("refresh", byEmail.getId(), byEmail.getRole().toString(), jwtConfig.getRefreshTokenValidityInSeconds());
        RefreshToken newRefreshToken = RefreshToken.builder()
                .refreshToken(refresh)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        response.setHeader("access-token", access);

        Cookie refreshCookie = new Cookie("refresh-token", refresh);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((jwtConfig.getRefreshTokenValidityInSeconds().intValue()));

        response.addCookie(refreshCookie);
    }


    private KaKaoOAuthTokenDTO getKaKaoOAuthTokenDTO(String accessCode) {

        return kaKaoOAuthClient.getToken(
                "authorization_code",
                kaKaoConfig.getClientId(),
                kaKaoConfig.getRedirectUri(),
                accessCode
        );
    }
}
