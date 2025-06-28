package or.sopt.houme.domain.user.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.sopt.houme.domain.user.client.KaKaoOAuthClient;
import or.sopt.houme.domain.user.client.KaKaoUserInfoClient;
import or.sopt.houme.domain.user.controller.dto.KaKaoOAuthTokenDTO;
import or.sopt.houme.domain.user.controller.dto.KaKaoUserInfoResponse;
import or.sopt.houme.domain.user.entity.Role;
import or.sopt.houme.domain.user.entity.SocialType;
import or.sopt.houme.domain.user.entity.User;
import or.sopt.houme.domain.user.repository.RefreshTokenRepository;
import or.sopt.houme.domain.user.repository.UserRepository;
import or.sopt.houme.global.config.JWTConfig;
import or.sopt.houme.global.config.KaKaoConfig;
import or.sopt.houme.global.jwt.JWTUtil;
import or.sopt.houme.global.util.CookieUtil;
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
     * 카카오 OAuth 인증을 위한 인가 코드 요청 URL을 생성하여 반환합니다.
     *
     * 이 메서드는 서버 측에서 OAuth 인증 로직의 유효성을 검증할 때 사용되며, 실제 클라이언트 리다이렉트에는 사용되지 않습니다.
     *
     * @return 카카오 인가 코드 요청을 위한 URL 문자열
     */
    public String requestRedirect() {
        return String.format(
                "https://kauth.kakao.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                kaKaoConfig.getClientId(), kaKaoConfig.getRedirectUri()
        );
    }


    /**
     * 카카오 OAuth 인가 코드를 이용해 로그인 처리를 수행합니다.
     *
     * 인가 코드를 통해 카카오 액세스 토큰을 발급받고, 해당 토큰으로 카카오 사용자 정보를 조회합니다.
     * 이메일 기준으로 기존 회원이 없으면 신규 회원을 생성합니다.
     * 이후 JWT 액세스 토큰과 리프레시 토큰을 발급하여, 액세스 토큰은 응답 헤더에, 리프레시 토큰은 보안 쿠키로 응답에 추가합니다.
     *
     * @param accessCode 카카오에서 발급받은 인가 코드
     * @param response   액세스/리프레시 토큰을 설정할 HTTP 응답 객체
     */
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

        refreshTokenRepository.saveRefreshToken(byEmail.getId(),refresh,jwtConfig.getRefreshTokenValidityInSeconds());

        response.setHeader("access-token", access);

        Cookie refreshCookie = CookieUtil.createSecureCookie("refresh-token",
                refresh,
                jwtConfig.getRefreshTokenValidityInSeconds().intValue(),
                false);

        response.addCookie(refreshCookie);
    }


    /**
     * 주어진 인가 코드를 사용하여 카카오 OAuth 토큰 정보를 조회합니다.
     *
     * @param accessCode 카카오에서 발급한 인가 코드
     * @return 카카오 OAuth 토큰 정보 DTO
     */
    private KaKaoOAuthTokenDTO getKaKaoOAuthTokenDTO(String accessCode) {

        return kaKaoOAuthClient.getToken(
                "authorization_code",
                kaKaoConfig.getClientId(),
                kaKaoConfig.getRedirectUri(),
                accessCode
        );
    }
}
